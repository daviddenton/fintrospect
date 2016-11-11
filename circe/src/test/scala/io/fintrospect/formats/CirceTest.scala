package io.fintrospect.formats

import com.twitter.finagle.Service
import com.twitter.finagle.http.Status.{Created, Ok}
import com.twitter.finagle.http.{Request, Status}
import com.twitter.util.Await.result
import com.twitter.util.{Await, Future}
import io.circe.generic.auto._
import io.fintrospect.formats.Circe.JsonFormat._
import io.fintrospect.formats.Circe.ResponseBuilder.implicits._
import io.fintrospect.formats.JsonFormat.InvalidJsonForDecoding
import io.fintrospect.parameters.{Body, Query}
import org.scalatest.{FunSpec, Matchers}

import scala.language.reflectiveCalls


case class CirceStreetAddress(address: String)

case class CirceLetter(to: CirceStreetAddress, from: CirceStreetAddress, message: String)

case class CirceLetterOpt(to: CirceStreetAddress, from: CirceStreetAddress, message: Option[String])

class CirceJsonResponseBuilderTest extends JsonResponseBuilderSpec(Circe)

class CirceFiltersTest extends FunSpec with Matchers {

  describe("Circe.Filters") {
    val aLetter = CirceLetter(CirceStreetAddress("my house"), CirceStreetAddress("your house"), "hi there")

    val request = Request()
    request.contentString = encode(aLetter).noSpaces

    describe("AutoInOut") {
      it("returns Ok") {
        val svc = Circe.Filters.AutoInOut(Service.mk { in: CirceLetter => Future.value(in) }, Created)

        val response = result(svc(request))
        response.status shouldBe Created
        decode[CirceLetter](parse(response.contentString)) shouldBe aLetter
      }
    }

    describe("AutoInOptionalOut") {
      it("returns Ok when present") {
        val svc = Circe.Filters.AutoInOptionalOut(Service.mk[CirceLetter, Option[CirceLetter]] { in => Future.value(Option(in)) })

        val response = result(svc(request))
        response.status shouldBe Ok
        decode[CirceLetter](parse(response.contentString)) shouldBe aLetter
      }

      it("returns NotFound when missing present") {
        val svc = Circe.Filters.AutoInOptionalOut(Service.mk[CirceLetter, Option[CirceLetter]] { in => Future.value(None) })
        result(svc(request)).status shouldBe Status.NotFound
      }
    }

    describe("AutoIn") {
      val svc = Circe.Filters.AutoIn(Circe.JsonFormat.body[CirceLetter]()).andThen(Service.mk { in: CirceLetter => Status.Ok(Circe.JsonFormat.encode(in)) })
      it("takes the object from the request") {
        decode[CirceLetter](parse(result(svc(request)).contentString)) shouldBe aLetter
      }

      it("rejects illegal content with a BadRequest") {
        val request = Request()
        request.contentString = "not xml"
        Await.result(svc(request)).status shouldBe Status.BadRequest
      }
    }

    describe("AutoOut") {
      it("takes the object from the request") {
        val svc = Circe.Filters.AutoOut[CirceLetter, CirceLetter](Created).andThen(Service.mk { in: CirceLetter => Future.value(in) })
        val response = result(svc(aLetter))
        response.status shouldBe Created
        decode[CirceLetter](parse(response.contentString)) shouldBe aLetter
      }
    }

    describe("AutoOptionalOut") {
      it("returns Ok when present") {
        val svc = Circe.Filters.AutoOptionalOut[CirceLetter, CirceLetter](Created).andThen(Service.mk[CirceLetter, Option[CirceLetter]] { in => Future.value(Option(in)) })

        val response = result(svc(aLetter))
        response.status shouldBe Created
        decode[CirceLetter](parse(response.contentString)) shouldBe aLetter
      }

      it("returns NotFound when missing present") {
        val svc = Circe.Filters.AutoOptionalOut[CirceLetter, CirceLetter](Created).andThen(Service.mk[CirceLetter, Option[CirceLetter]] { in => Future.value(None) })
        result(svc(aLetter)).status shouldBe Status.NotFound
      }
    }
  }
}

case class CirceWithOptionalFields(from: Option[String])

class CirceJsonFormatTest extends JsonFormatSpec(Circe.JsonFormat) {

  import io.circe.generic.auto._

  describe("Circe.JsonFormat") {
    val aLetter = CirceLetter(CirceStreetAddress("my house"), CirceStreetAddress("your house"), "hi there")

    it("roundtrips to JSON and back") {
      val encoded = encode(aLetter)
      decode[CirceLetter](encoded) shouldBe aLetter
    }

    it("patchbody modifies original object with a non-null value") {
      val original = CirceLetterOpt(CirceStreetAddress("my house"), CirceStreetAddress("your house"), None)
      val modifier = encode(obj("message" -> string("hi there")))
      val modifyLetter = patcher[CirceLetterOpt](modifier)
      modifyLetter(original) shouldBe CirceLetterOpt(CirceStreetAddress("my house"), CirceStreetAddress("your house"), Option("hi there"))
    }

    // wait for circe 0.6.X, where this bug will be fixed - https://github.com/travisbrown/circe/issues/304
    ignore("patcher modifies original object with a null value") {
      val original = CirceLetterOpt(CirceStreetAddress("my house"), CirceStreetAddress("your house"), Option("hi there"))
      val modifier = encode(obj())
      val modifyLetter = patcher[CirceLetterOpt](modifier)
      modifyLetter(original) shouldBe CirceLetterOpt(CirceStreetAddress("my house"), CirceStreetAddress("your house"), None)
    }

    it("invalid extracted JSON throws up") {
      intercept[InvalidJsonForDecoding](decode[CirceLetter](Circe.JsonFormat.obj()))
    }

    it("body spec decodes content") {
      (Body(bodySpec[CirceLetter]()) <-- Circe.ResponseBuilder.OK(encode(aLetter)).build()) shouldBe aLetter
    }

    it("patch body can be used to modify an existing case class object") {
      val letterWithNoMessage = CirceLetterOpt(CirceStreetAddress("my house"), CirceStreetAddress("your house"), None)
      val modifiedMessage = encode(obj("message" -> string("hi there")))
      val modifiedLetterWithMessage = CirceLetterOpt(CirceStreetAddress("my house"), CirceStreetAddress("your house"), Some("hi there"))

      val patch = patchBody[CirceLetterOpt](None, modifiedLetterWithMessage) <-- Circe.ResponseBuilder.OK(modifiedMessage).build()

      patch(letterWithNoMessage) shouldBe modifiedLetterWithMessage
    }

    it("response spec has correct code") {
      Circe.JsonFormat.responseSpec[CirceLetter](Ok -> "ok", aLetter).status shouldBe Ok
    }

    it("param spec decodes content") {
      val param = Query.required(parameterSpec[CirceLetter]("name"))
      (param <-- Request("?name=" + encode(aLetter))) shouldBe aLetter
    }
  }
  override val expectedJson: String = """{"string":"hello","object":{"field1":"aString"},"int":1.0,"long":2,"decimal":1.2,"bigInt":12344,"bool":true,"null":null,"array":["world",true]}"""
}
