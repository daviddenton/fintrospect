package io.fintrospect.util

import java.time.Duration.ofSeconds
import java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME
import java.time.{Duration, ZonedDateTime}

import com.twitter.finagle.Service
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.util.Await.result
import com.twitter.util.Future
import io.fintrospect.configuration.{Authority, Credentials, Host, Port}
import io.fintrospect.util.Filters.Request.{AddHost, BasicAuthorization}
import io.fintrospect.util.Filters.Response.{AddDate, CatchAll, ReportingRouteLatency}
import io.fintrospect.util.HttpRequestResponseUtil.headerOf
import io.fintrospect.util.TestClocks._
import io.fintrospect.{ContentTypes, Headers}
import org.scalatest.{FunSpec, ShouldMatchers}

class FiltersTest extends FunSpec with ShouldMatchers {
  describe("Request") {

    describe("Add authority host header") {
      it("works") {
        val authority = Authority(Host.localhost, Port(9865))

        result(AddHost(authority)(Request(), Service.mk { req => Future.value(headerOf("Host")(req)) })) shouldBe authority.toString
      }
    }

    describe("Add basic authorization header") {
      it("works") {
        result(BasicAuthorization(Credentials("hello", "kitty"))(Request(), Service.mk { req => Future.value(headerOf("Authorization")(req)) })) shouldBe "Basic aGVsbG86a2l0dHk="
      }
    }

    describe("Adds Accept header") {
      it("works") {
        result(Filters.Request.AddAccept(ContentTypes.APPLICATION_ATOM_XML, ContentTypes.APPLICATION_JSON)(Request(), Service.mk { req => Future.value(headerOf("Accept")(req)) })) shouldBe "application/atom+xml, application/json"
      }
    }
  }

  describe("Response") {
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
  }
}
