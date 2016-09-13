package io.fintrospect.formats

import com.twitter.finagle.Service
import com.twitter.finagle.http.Status.{Created, NotFound, Ok}
import com.twitter.finagle.http.{Request, Status}
import com.twitter.util.Await.result
import com.twitter.util.{Await, Future}
import io.fintrospect.formats.Xml.ResponseBuilder.implicits._
import io.fintrospect.parameters.Body
import org.scalatest.{FunSpec, Matchers}

import scala.xml.{Elem, XML}

class XmlFiltersTest extends FunSpec with Matchers {

  describe("Xml.Filters") {

    val request = Request()
    request.contentString = <xml></xml>.toString()

    describe("AutoInOut") {
      val svc = Xml.Filters.AutoInOut(Service.mk { in: Elem => Future.value(in) }, Created)

      it("returns Ok") {
        val response = result(svc(request))
        response.status shouldBe Created
        XML.loadString(response.contentString) shouldBe <xml></xml>
      }
    }

    describe("AutoInOptionalOut") {
      it("returns Ok when present") {
        val svc = Xml.Filters.AutoInOptionalOut(Service.mk[Elem, Option[Elem]] { in => Future.value(Option(in)) })

        val response = result(svc(request))
        response.status shouldBe Ok
        XML.loadString(response.contentString) shouldBe <xml></xml>
      }

      it("returns NotFound when missing present") {
        val svc = Xml.Filters.AutoInOptionalOut(Service.mk[Elem, Option[Elem]] { in => Future.value(None) })
        result(svc(request)).status shouldBe NotFound
      }
    }

    describe("AutoIn") {
      val svc = Xml.Filters.AutoIn(Body.xml(None)).andThen(Service.mk { in: Elem => Status.Ok(in) })
      it("takes the object from the request") {
        XML.loadString(result(svc(request)).contentString) shouldBe <xml></xml>
      }

      it("rejects illegal content with a BadRequest") {
        val request = Request()
        request.contentString = "not xml"
        Await.result(svc(request)).status shouldBe Status.BadRequest
      }
    }

    describe("AutoOut") {
      it("takes the object from the request") {
        val svc = Xml.Filters.AutoOut[Elem](Created).andThen(Service.mk { in: Elem => Future.value(in) })
        val response = result(svc(<xml></xml>))
        response.status shouldBe Created
        XML.loadString(response.contentString) shouldBe <xml></xml>
      }
    }

    describe("AutoOptionalOut") {
      it("returns Ok when present") {
        val svc = Xml.Filters.AutoOptionalOut[Elem](Created).andThen(Service.mk[Elem, Option[Elem]] { in => Future.value(Option(in)) })

        val response = result(svc(<xml></xml>))
        response.status shouldBe Created
        XML.loadString(response.contentString) shouldBe <xml></xml>
      }

      it("returns NotFound when missing present") {
        val svc = Xml.Filters.AutoOptionalOut[Elem](Created).andThen(Service.mk[Elem, Option[Elem]] { in => Future.value(None) })
        result(svc(<xml></xml>)).status shouldBe NotFound
      }
    }
  }
}
class XmlResponseBuilderTest extends AbstractResponseBuilderSpec(Xml.ResponseBuilder) {
  override val expectedContent = message
  override val customError = <message>{message}</message>
  override val expectedErrorContent = s"<message>$message</message>"
  override val customType = <okThing>theMessage</okThing>
  override val customTypeSerialised: String = customType.toString()
}
