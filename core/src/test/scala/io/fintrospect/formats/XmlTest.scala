package io.fintrospect.formats

import com.twitter.finagle.Service
import com.twitter.finagle.http.{Request, Status}
import com.twitter.util.Await.result
import com.twitter.util.{Await, Future}
import io.fintrospect.formats.Xml.ResponseBuilder._
import io.fintrospect.parameters.Body
import org.scalatest.{FunSpec, Matchers}

import scala.xml.{Elem, XML}

class XmlFiltersTest extends FunSpec with Matchers {

  describe("Xml.Filters") {

    val request = Request()
    request.contentString = <xml></xml>.toString()

    describe("AutoInOut") {
      val svc = Xml.Filters.AutoInOut(Status.Created).andThen(Service.mk { in: Elem => Future(in) })

      it("returns Ok") {
        val response = result(svc(request))
        response.status shouldBe Status.Created
        XML.loadString(response.contentString) shouldBe <xml></xml>
      }
    }

    describe("AutoInOptionalOut") {
      it("returns Ok when present") {
        val svc = Xml.Filters.AutoInOptionalOut().andThen(Service.mk[Elem, Option[Elem]] { in => Future(Option(in)) })

        val response = result(svc(request))
        response.status shouldBe Status.Ok
        XML.loadString(response.contentString) shouldBe <xml></xml>
      }

      it("returns NotFound when missing present") {
        val svc = Xml.Filters.AutoInOptionalOut().andThen(Service.mk[Elem, Option[Elem]] { _ => Future(None) })
        result(svc(request)).status shouldBe Status.NotFound
      }
    }

    describe("AutoIn") {
      val svc = Xml.Filters.AutoIn().andThen(Service.mk { in: Elem => Ok(in) })
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
        val svc = Xml.Filters.AutoOut(Status.Created).andThen(Service.mk { in: Elem => Future(in) })
        val response = result(svc(<xml></xml>))
        response.status shouldBe Status.Created
        XML.loadString(response.contentString) shouldBe <xml></xml>
      }
    }

    describe("AutoOptionalOut") {
      it("returns Ok when present") {
        val svc = Xml.Filters.AutoOptionalOut(Status.Created).andThen(Service.mk[Elem, Option[Elem]] { in => Future(Option(in)) })

        val response = result(svc(<xml></xml>))
        response.status shouldBe Status.Created
        XML.loadString(response.contentString) shouldBe <xml></xml>
      }

      it("returns NotFound when missing present") {
        val svc = Xml.Filters.AutoOptionalOut(Status.Created).andThen(Service.mk[Elem, Option[Elem]] { in => Future(None) })
        result(svc(<xml></xml>)).status shouldBe Status.NotFound
      }
    }
  }
}

class XmlResponseBuilderTest extends ResponseBuilderSpec(Xml.ResponseBuilder) {
  override val customError = <message>{message}</message>
  override val customErrorSerialized = s"<message>$message</message>"
  override val customType = <okThing>theMessage</okThing>
  override val customTypeSerialized: String = customType.toString()
}



