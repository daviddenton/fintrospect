package io.fintrospect.util

import java.time.Duration.ofSeconds
import java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME
import java.time.{Duration, ZonedDateTime}

import com.twitter.finagle.Service
import com.twitter.finagle.http.Status.{BadRequest, NotAcceptable, Ok}
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.util.Await.result
import com.twitter.util.{Await, Future}
import io.fintrospect.ContentTypes.{APPLICATION_XHTML_XML, APPLICATION_XML, WILDCARD}
import io.fintrospect.configuration.{Authority, Credentials, Host, Port}
import io.fintrospect.formats.PlainText.ResponseBuilder.implicits._
import io.fintrospect.parameters.{Extracted, ExtractionFailed, Extractor, NotProvided, Query}
import io.fintrospect.util.Filters.Request.{AddHost, BasicAuthorization, StrictAccept}
import io.fintrospect.util.Filters.Response.{AddDate, CatchAll, ReportingRouteLatency}
import io.fintrospect.util.HttpRequestResponseUtil.headerOf
import io.fintrospect.util.TestClocks._
import io.fintrospect.{ContentTypes, Headers}
import org.scalatest.{FunSpec, ShouldMatchers}

class FiltersTest extends FunSpec with ShouldMatchers {

  describe("Request") {

    describe("ExtractingRequest") {
      it("when extracts request object successfully, passes through to service") {
        val message = "hello"

        val filter = Filters.Request.ExtractingRequest {
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

        val filter = Filters.Request.ExtractingRequest[String] {
          req => ExtractionFailed(Seq())
        }
        val response = result(filter(Request(), Service.mk { message => Ok(message) }))

        response.status shouldBe BadRequest
      }

      it("when extraction fails with no object at all then return bad request") {
        val filter = Filters.Request.ExtractingRequest[String] {
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
        val f = Filters.Request.Tap { r => req = Some(r) }
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
        result(Filters.Request.AddAccept(ContentTypes.APPLICATION_ATOM_XML, ContentTypes.APPLICATION_JSON)(Request(), Service.mk { req => Future.value(headerOf("Accept")(req)) })) shouldBe "application/atom+xml, application/json"
      }
    }
  }

  describe("Response") {

    describe("ExtractingResponse") {
      it("when extracts response object successfully") {
        val message = "hello"

        val filter = Filters.Response.ExtractingResponse {
          req => Extracted(message)
        }

        val response = result(filter(Request(), Service.mk { message => Future.value(Response()) }))

        response match {
          case Extracted(s) => s shouldBe message
          case _ => fail("did not pass")
        }
      }

      it("when extraction fails with no object at all") {
        val filter = Filters.Response.ExtractingResponse {
          req => NotProvided
        }
        val response = result(filter(Request(), Service.mk { message => Future.value(Response()) }))

        response match {
          case NotProvided =>
          case _ => fail("did not pass")
        }
      }
    }

    describe("CatchAll") {
      it("converts uncaught exceptions into 500 responses") {
        val rsp = result(CatchAll()(Request(), Service.mk { req: Request => Future.exception(new RuntimeException("boo")) }))
        rsp.status shouldBe Status.InternalServerError
        rsp.contentString shouldBe """{"message":"boo"}"""
      }
    }

    describe("Add date header") {
      it("works") {
        val response = result(AddDate(fixed)(Request(), Service.mk { req: Request => Future.value(Response()) }))
        headerOf("Date")(response) shouldBe RFC_1123_DATE_TIME.format(ZonedDateTime.now(fixed))
      }
    }

    describe("reporting request latency") {
      it("for unknown path") {
        var called: (String, Duration) = null

        val filter = ReportingRouteLatency(ticking) { (name: String, duration: Duration) => called = (name, duration) }

        result(filter(Request(), Service.mk { req => Future.value(Response()) }))

        called shouldBe("GET.UNMAPPED.2xx.200", ofSeconds(1))
      }

      it("for known path (with Identity header)") {
        var called: (String, Duration) = null

        val filter = ReportingRouteLatency(ticking) { (name: String, duration: Duration) => called = (name, duration) }

        val request = Request("/")

        request.headerMap(Headers.IDENTIFY_SVC_HEADER) = "GET:/path/dir/someFile.html"

        result(filter(request, Service.mk { req => Future.value(Response()) }))

        called shouldBe("GET._path_dir_someFile_html.2xx.200", ofSeconds(1))
      }
    }

    describe("Tap") {
      it("feeds the response into the defined function after receiving it from the service") {
        val request = Request()
        val response = Response()

        var resp: Option[Response] = None
        val f = Filters.Response.Tap { r => resp = Some(r) }
          .andThen(Service.mk { r: Request =>
            Future.value(response)
          })
        Await.result(f(request)) shouldBe response
        resp shouldBe Some(response)
      }
    }

    describe("TapFailure") {
      it("feeds the exception into the defined function after receiving it from the service") {
        val request = Request()
        val response = Response()

        val e = new scala.RuntimeException()

        var fed: Option[Throwable] = None
        val f = Filters.Response.TapFailure { r => fed = Some(r) }
          .andThen(Service.mk { r: Request =>
            Future.exception(e)
          })
        intercept[RuntimeException](Await.result(f(request))) shouldBe e
        fed shouldBe Some(e)
      }
    }


  }

  describe("Misc") {
    it("PrintRequestAndResponse") {
      Await.result(Filters.PrintRequestAndResponse.andThen((_: Request) => Future.value(Response()))(Request()))
    }
  }

}
