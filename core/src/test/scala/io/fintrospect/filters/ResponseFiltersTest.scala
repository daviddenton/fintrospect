package io.fintrospect.filters

import java.time.Duration.ofSeconds
import java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME
import java.time.{Duration, ZonedDateTime}

import com.twitter.finagle.Service
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.util.Await.result
import com.twitter.util.{Await, Future}
import io.fintrospect.Headers
import io.fintrospect.filters.ResponseFilters._
import io.fintrospect.util.Extracted
import io.fintrospect.util.HttpRequestResponseUtil.headerOf
import io.fintrospect.util.TestClocks._
import org.scalatest.{FunSpec, Matchers}

class ResponseFiltersTest extends FunSpec with Matchers {

  describe("Response") {

    describe("ExtractingResponse") {
      it("when extracts response object successfully") {
        val message = "hello"

        val filter = ResponseFilters.ExtractingResponse {
          req => Extracted(Some(message))
        }

        val response = result(filter(Request(), Service.mk { message => Future.value(Response()) }))

        response match {
          case Extracted(Some(s)) => s shouldBe message
          case _ => fail("did not pass")
        }
      }

      it("when extraction fails with no object at all") {
        val filter = ResponseFilters.ExtractingResponse {
          req => Extracted(None)
        }
        val response = result(filter(Request(), Service.mk { message => Future.value(Response()) }))

        response match {
          case Extracted(None) =>
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
      val clock = fixed()

      it("works") {
        val response = result(AddDate(clock)(Request(), Service.mk { req: Request => Future.value(Response()) }))
        headerOf("Date")(response) shouldBe RFC_1123_DATE_TIME.format(ZonedDateTime.now(clock))
      }
    }

    describe("reporting request latency") {
      it("for unknown path") {
        var called: (String, Duration) = null

        val filter = ReportingRouteLatency(ticking) { (name: String, duration: Duration) => called = (name, duration) }

        val response = Response()

        result(filter(Request(), Service.mk { req => Future.value(response) })) shouldBe response

        called shouldBe("GET.UNMAPPED.2xx.200", ofSeconds(1))
      }

      it("for known path (with Identity header)") {
        var called: (String, Duration) = null

        val filter = ReportingRouteLatency(ticking) { (name: String, duration: Duration) => called = (name, duration) }

        val request = Request("/")
        val response = Response()

        request.headerMap(Headers.IDENTIFY_SVC_HEADER) = "GET:/path/dir/someFile.html"

        result(filter(request, Service.mk { req => Future.value(response) })) shouldBe response

        called shouldBe("GET._path_dir_someFile_html.2xx.200", ofSeconds(1))
      }
    }

    describe("Tap") {
      it("feeds the response into the defined function after receiving it from the service") {
        val request = Request()
        val response = Response()

        var resp: Option[Response] = None
        val f = ResponseFilters.Tap { r => resp = Some(r) }
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

        val e = new scala.RuntimeException()

        var fed: Option[Throwable] = None
        val f = ResponseFilters.TapFailure { r => fed = Some(r) }
          .andThen(Service.mk { r: Request =>
            Future.exception(e)
          })
        intercept[RuntimeException](Await.result(f(request))) shouldBe e
        fed shouldBe Some(e)
      }
    }
  }

}
