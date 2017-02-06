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
import io.fintrospect.formats.Xml
import io.fintrospect.parameters.Body
import io.fintrospect.util.HttpRequestResponseUtil.headerOf
import io.fintrospect.util.TestClocks._
import io.fintrospect.util.{Extracted, ExtractionFailed}
import org.scalatest.{FunSpec, Matchers}

class ResponseFiltersTest extends FunSpec with Matchers {

  describe("Response") {

    describe("ExtractingResponse with Function") {
      it("when extracts response object successfully") {
        val message = "hello"

        val filter = ResponseFilters.ExtractBody { (_: Response) => Extracted(message) }

        result(filter(Request(), Service.mk { message => Future(Response()) })) shouldBe Extracted(Some(message))
      }

      it("when extraction fails with no object at all") {
        val filter = ResponseFilters.ExtractBody {
          (_: Response) => Extracted("hello")
        }
        result(filter(Request(), Service.mk { _ => Future(Response()) })) shouldBe Extracted(Some("hello"))
      }
    }

    describe("ExtractingResponse with Extractor") {
      it("when extracts response successfully") {

        val filter = ResponseFilters.ExtractBody(Body.xml())

        result(filter(Request(), Service.const(Xml.ResponseBuilder.Ok(<xml/>)))) shouldBe Extracted(Some(<xml/>))
      }

      it("default predicate is NotFound") {
        val filter = ResponseFilters.ExtractBody(Body.xml())

        result(filter(Request(), Service.const(Xml.ResponseBuilder.NotFound("")))) shouldBe Extracted(None)
      }

      it("when predicate fails") {
        val filter = ResponseFilters.ExtractBody(Body.xml(), _.status != Status.Ok)

        result(filter(Request(), Service.const(Xml.ResponseBuilder.Ok("")))) shouldBe Extracted(None)
      }

      it("when extraction fails") {
        val filter = ResponseFilters.ExtractBody(Body.xml())

        val response = result(filter(Request(), Service.const(Xml.ResponseBuilder.Ok(""))))

        response match {
          case ExtractionFailed(_) =>
          case _ => fail("extraction not as expected")
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
        val response = result(AddDate[Request](clock)(Request(), Service.mk { req: Request => Future(Response()) }))
        headerOf("Date")(response) shouldBe RFC_1123_DATE_TIME.format(ZonedDateTime.now(clock))
      }
    }

    describe("reporting request latency") {
      it("for unknown path") {
        var called: (String, Duration) = null

        val filter = ReportRouteLatency(ticking) { (name: String, duration: Duration) => called = (name, duration) }

        val response = Response()

        result(filter(Request(), Service.mk { req => Future(response) })) shouldBe response

        called shouldBe("GET.UNMAPPED.2xx.200", ofSeconds(1))
      }

      it("for known path (with Identity header)") {
        var called: (String, Duration) = null

        val filter = ReportRouteLatency(ticking) { (name: String, duration: Duration) => called = (name, duration) }

        val request = Request("/")
        val response = Response()

        request.headerMap(Headers.IDENTIFY_SVC_HEADER) = "GET:/path/dir/someFile.html"

        result(filter(request, Service.mk { req => Future(response) })) shouldBe response

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
            Future(response)
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
