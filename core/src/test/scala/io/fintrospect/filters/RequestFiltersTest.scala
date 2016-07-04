package io.fintrospect.filters

import com.twitter.finagle.Service
import com.twitter.finagle.http.Status.{BadRequest, NotAcceptable, Ok}
import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.Await.result
import com.twitter.util.{Await, Future}
import io.fintrospect.ContentTypes
import io.fintrospect.ContentTypes.{APPLICATION_XHTML_XML, APPLICATION_XML, WILDCARD}
import io.fintrospect.configuration.{Authority, Credentials, Host, Port}
import io.fintrospect.filters.RequestFilters.{AddHost, BasicAuthorization, StrictAccept}
import io.fintrospect.formats.PlainText.ResponseBuilder.implicits._
import io.fintrospect.parameters.{Extracted, ExtractionFailed, Extractor, NotProvided, Query}
import io.fintrospect.util.HttpRequestResponseUtil.headerOf
import org.scalatest.{FunSpec, ShouldMatchers}

class RequestFiltersTest extends FunSpec with ShouldMatchers {

  describe("Request") {

    describe("ExtractingRequest") {
      it("when extracts request object successfully, passes through to service") {
        val message = "hello"

        val filter = RequestFilters.ExtractingRequest {
          req => Extracted(message)
        }
        val response = result(filter(Request(), Service.mk { message => Ok(message) }))

        response.status shouldBe Ok
        response.contentString shouldBe message
      }

      it("when extract fails normally then return bad request") {

        Extractor.mk {
          r: Request => for {
            a <- Query.optional.string("bob") <--? r
          } yield None
        }

        val filter = RequestFilters.ExtractingRequest[String] {
          req => ExtractionFailed(Nil)
        }
        val response = result(filter(Request(), Service.mk { message => Ok(message) }))

        response.status shouldBe BadRequest
      }

      it("when extraction fails with no object at all then return bad request") {
        val filter = RequestFilters.ExtractingRequest[String] {
          req => NotProvided
        }
        val response = result(filter(Request(), Service.mk { message => Ok(message) }))

        response.status shouldBe BadRequest
      }
    }

    describe("Tap") {
      it("feeds the request into the defined function before sending it to service") {
        val request = Request()
        val response = Response()

        var req: Option[Request] = None
        val f = RequestFilters.Tap { r => req = Some(r) }
          .andThen(Service.mk { r: Request =>
            Future.value(response)
          })
        Await.result(f(request)) shouldBe response
        req shouldBe Some(request)
      }
    }

    describe("StrictAccept") {
      it("passes through when no accept header") {
        result(StrictAccept(APPLICATION_XML)(Request(), Service.mk { req => Future.value(Response()) })).status shouldBe Ok
      }

      it("passes through when wildcard accept header") {
        val request = Request()
        request.headerMap("Accept") = WILDCARD.value
        result(StrictAccept(APPLICATION_XML)(request, Service.mk { req => Future.value(Response()) })).status shouldBe Ok
      }

      it("passes through when correct accept header") {
        val request = Request()
        request.headerMap("Accept") = APPLICATION_XML.value
        result(StrictAccept(APPLICATION_XML)(request, Service.mk { req => Future.value(Response()) })).status shouldBe Ok
      }

      it("Not Acceptable when wrong accept header") {
        val request = Request()
        request.headerMap("Accept") = APPLICATION_XHTML_XML.value
        result(StrictAccept(APPLICATION_XML)(request, Service.mk { req => Future.value(Response()) })).status shouldBe NotAcceptable
      }
    }

    describe("AddHost") {
      it("adds authority host header") {
        val authority = Authority(Host.localhost, Port(9865))
        result(AddHost(authority)(Request(), Service.mk { req => Future.value(headerOf("Host")(req)) })) shouldBe authority.toString
      }
    }

    describe("BasicAuthorization") {
      it("adds basic authorization header") {
        result(BasicAuthorization(Credentials("hello", "kitty"))(Request(), Service.mk { req => Future.value(headerOf("Authorization")(req)) })) shouldBe "Basic aGVsbG86a2l0dHk="
      }
    }

    describe("AddAccept") {
      it("adds accept header") {
        result(RequestFilters.AddAccept(ContentTypes.APPLICATION_ATOM_XML, ContentTypes.APPLICATION_JSON)(Request(), Service.mk { req => Future.value(headerOf("Accept")(req)) })) shouldBe "application/atom+xml, application/json"
      }
    }
  }

}
