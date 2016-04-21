package io.fintrospect.util

import java.time.Clock.tickSeconds
import java.time.{Instant, ZoneId, Clock, Duration}
import java.time.Duration.ofSeconds
import java.time.ZoneId.systemDefault

import com.twitter.finagle.Service
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.util.Await.result
import com.twitter.util.Future
import io.fintrospect.Headers
import io.fintrospect.configuration.{Authority, Credentials, Host, Port}
import io.fintrospect.util.Filters.{addAuthorityHost, addBasicAuthorization, reportingPathAndLatency}
import org.scalatest.{FunSpec, ShouldMatchers}

class FiltersTest extends FunSpec with ShouldMatchers {

  describe("Add authority host header") {
    it("works") {
      val authority = Authority(Host.localhost, Port(9865))

      result(addAuthorityHost(authority)(Request(), Service.mk { req => Future.value(req.headerMap("Host")) })) shouldBe authority.toString
    }
  }

  describe("Add basic authorization header") {
    it("works") {
      result(addBasicAuthorization(Credentials("hello", "kitty"))(Request(), Service.mk { req => Future.value(req.headerMap("Authorization")) })) shouldBe "Basic aGVsbG86a2l0dHk="
    }
  }

  describe("Add eTag") {
    it("passes through when predicate succeeds") {
      val response: Response = Response()
      response.contentString = "bob"
      result(Filters.addETag[Request]()(Request(), Service.mk { req => Future.value(response) })).headerMap("ETag") shouldBe "9f9d51bc70ef21ca5c14f307980a29d8"
    }
  }

 private val clock = new Clock {
    private var current = Instant.now()
    override def getZone: ZoneId = systemDefault()

    override def instant(): Instant = {
      current = current.plusSeconds(1)
      current
    }

    override def withZone(zone: ZoneId): Clock = this
  }

  describe("reporting request latency") {
    it("for unknown path") {
      var called: (String, Duration) = null

      val filter = reportingPathAndLatency(clock) { (name: String, duration: Duration) => called = (name, duration) }

      result(filter(Request(), Service.mk { req => Future.value(Response()) }))

      called shouldBe("GET.UNMAPPED.2xx.200", ofSeconds(1))
    }

    it("for known path (with Identity header)") {
      var called: (String, Duration) = null

      val filter = reportingPathAndLatency(clock) { (name: String, duration: Duration) => called = (name, duration) }

      val request = Request("/")
      request.headerMap.add(Headers.IDENTIFY_SVC_HEADER, "GET:/path/dir/someFile.html")

      result(filter(request, Service.mk { req => Future.value(Response()) }))

      called shouldBe("GET._path_dir_someFile_html.2xx.200", ofSeconds(1))
    }
  }

}
