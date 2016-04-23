package io.fintrospect.util

import java.time.Duration.ofSeconds
import java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME
import java.time.{Duration, ZonedDateTime}

import com.twitter.finagle.Service
import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.Await.result
import com.twitter.util.Future
import io.fintrospect.{ContentTypes, ContentType, Headers}
import io.fintrospect.configuration.{Authority, Credentials, Host, Port}
import io.fintrospect.util.Filters.Request.{AddHost, BasicAuthorization}
import io.fintrospect.util.Filters.Response.{AddDate, ReportingRouteLatency}
import io.fintrospect.util.TestClocks._
import org.scalatest.{FunSpec, ShouldMatchers}

class FiltersTest extends FunSpec with ShouldMatchers {
  describe("Request") {

    describe("Add authority host header") {
      it("works") {
        val authority = Authority(Host.localhost, Port(9865))

        result(AddHost(authority)(Request(), Service.mk { req => Future.value(req.headerMap("Host")) })) shouldBe authority.toString
      }
    }

    describe("Add basic authorization header") {
      it("works") {
        result(BasicAuthorization(Credentials("hello", "kitty"))(Request(), Service.mk { req => Future.value(req.headerMap("Authorization")) })) shouldBe "Basic aGVsbG86a2l0dHk="
      }
    }

    describe("Adds Content-Type") {
      it("works") {
        result(Filters.Request.AddContentType(ContentTypes.APPLICATION_ATOM_XML)(Request(), Service.mk { req => Future.value(req.headerMap("Content-Type")) })) shouldBe "application/atom+xml"
      }
    }
  }

  describe("Response") {

    describe("Add date header") {
      it("works") {
        result(AddDate(fixed)(Request(), Service.mk { req:Request => Future.value(Response()) })).headerMap("Date") shouldBe RFC_1123_DATE_TIME.format(ZonedDateTime.now(fixed))
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
