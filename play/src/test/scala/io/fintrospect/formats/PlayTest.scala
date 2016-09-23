package io.fintrospect.formats

import com.twitter.finagle.Service
import com.twitter.finagle.http.Status.{Created, Ok}
import com.twitter.finagle.http.{Request, Status}
import com.twitter.util.Await.result
import com.twitter.util.{Await, Future}
import io.fintrospect.formats.JsonFormat.InvalidJsonForDecoding
import io.fintrospect.formats.Play.JsonFormat.{bodySpec, decode, encode, parameterSpec, parse}
import io.fintrospect.formats.Play.ResponseBuilder.implicits._
import io.fintrospect.parameters.{Body, Query}
import org.scalatest.{FunSpec, Matchers}
import play.api.libs.json._

import scala.language.reflectiveCalls

case class PlayStreetAddress(address: String)

object PlayStreetAddress {
  implicit val Writes = new Writes[PlayStreetAddress] {
    override def writes(in: PlayStreetAddress) = JsObject(Seq("address" -> JsString(in.address)))
  }
  implicit val Reads = new Reads[PlayStreetAddress] {
    override def reads(in: JsValue) = JsSuccess(PlayStreetAddress(
      (in \ "address").as[String]
    ))
  }
}

case class PlayLetter(to: PlayStreetAddress, from: PlayStreetAddress, message: String)

object PlayLetter {

  implicit val R = PlayStreetAddress.Reads
  implicit val W = PlayStreetAddress.Writes

  implicit val Writes = Json.writes[PlayLetter]
  implicit val Reads = Json.reads[PlayLetter]
}

class PlayFiltersTest extends FunSpec with Matchers {

  describe("Play.Filters") {
    val aLetter = PlayLetter(PlayStreetAddress("my house"), PlayStreetAddress("your house"), "hi there")

    val request = Request()
    request.contentString = Play.JsonFormat.compact(encode(aLetter))

    describe("AutoInOut") {
      it("returns Ok") {
        val svc = Play.Filters.AutoInOut(Service.mk { in: PlayLetter => Future.value(in) }, Created)

        val response = result(svc(request))
        response.status shouldBe Created
        decode[PlayLetter](parse(response.contentString)) shouldBe aLetter
      }
    }

    describe("AutoInOptionalOut") {
      it("returns Ok when present") {
        val svc = Play.Filters.AutoInOptionalOut(Service.mk[PlayLetter, Option[PlayLetter]] { in => Future.value(Option(in)) })

        val response = result(svc(request))
        response.status shouldBe Status.Ok
        decode[PlayLetter](parse(response.contentString)) shouldBe aLetter
      }

      it("returns NotFound when missing present") {
        val svc = Play.Filters.AutoInOptionalOut(Service.mk[PlayLetter, Option[PlayLetter]] { in => Future.value(None) })
        result(svc(request)).status shouldBe Status.NotFound
      }
    }

    describe("AutoIn") {
      val svc = Play.Filters.AutoIn(Play.JsonFormat.body[PlayLetter]()).andThen(Service.mk { in: PlayLetter => Ok(Play.JsonFormat.encode(in)) })

      it("takes the object from the request") {
        Play.JsonFormat.body[PlayLetter]() <-- result(svc(request)) shouldBe aLetter
      }

      it("rejects illegal content with a BadRequest") {
        val request = Request()
        request.contentString = "not xml"
        Await.result(svc(request)).status shouldBe Status.BadRequest
      }
    }

    describe("AutoOut") {
      it("takes the object from the request") {
        val svc = Play.Filters.AutoOut[PlayLetter, PlayLetter](Created).andThen(Service.mk { in: PlayLetter => Future.value(in) })
        val response = result(svc(aLetter))
        response.status shouldBe Created
        decode[PlayLetter](parse(response.contentString)) shouldBe aLetter
      }
    }

    describe("AutoOptionalOut") {
      it("returns Ok when present") {
        val svc = Play.Filters.AutoOptionalOut[PlayLetter, PlayLetter](Created).andThen(Service.mk[PlayLetter, Option[PlayLetter]] { in => Future.value(Option(in)) })

        val response = result(svc(aLetter))
        response.status shouldBe Created
        decode[PlayLetter](parse(response.contentString)) shouldBe aLetter
      }

      it("returns NotFound when missing present") {
        val svc = Play.Filters.AutoOptionalOut[PlayLetter, PlayLetter](Created).andThen(Service.mk[PlayLetter, Option[PlayLetter]] { in => Future.value(None) })
        result(svc(aLetter)).status shouldBe Status.NotFound
      }
    }
  }
}
class PlayJsonResponseBuilderTest extends JsonResponseBuilderSpec(Play)

class PlayJsonFormatTest extends JsonFormatSpec(Play.JsonFormat) {

  describe("Play.JsonFormat") {
    val aLetter = PlayLetter(PlayStreetAddress("my house"), PlayStreetAddress("your house"), "hi there")

    it("roundtrips to JSON and back") {
      val encoded = Play.JsonFormat.encode(aLetter)(PlayLetter.Writes)
      Play.JsonFormat.decode[PlayLetter](encoded)(PlayLetter.Reads) shouldBe aLetter
    }

    it("invalid extracted JSON throws up") {
      intercept[InvalidJsonForDecoding](Play.JsonFormat.decode[PlayLetter](Play.JsonFormat.obj()))
    }

    it("body spec decodes content") {
      (Body(bodySpec[PlayLetter]()) <-- Play.ResponseBuilder.OK(encode(aLetter)).build()) shouldBe aLetter
    }

    it("param spec decodes content") {
      val param = Query.required(parameterSpec[PlayLetter]("name"))
      (param <-- Request("?name=" + encode(aLetter))) shouldBe aLetter
    }
  }

}
