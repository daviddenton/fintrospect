package io.fintrospect.filters

import java.time.Duration.ofSeconds
import java.time.format.DateTimeFormatter._
import java.time.{Duration, ZonedDateTime}

import com.twitter.finagle.http.{Method, Request, Response, Status}
import com.twitter.finagle.{Filter, Service}
import com.twitter.util.Await.result
import com.twitter.util.{Await, Future}
import io.fintrospect.filters.Caching.Response.FallbackCacheControl
import io.fintrospect.filters.Caching.{DefaultCacheTimings, MaxAgeTtl, StaleIfErrorTtl, StaleWhenRevalidateTtl}
import io.fintrospect.util.HttpRequestResponseUtil.headerOf
import io.fintrospect.util.TestClocks.fixed
import org.scalatest.{FunSpec, Matchers}

class CachingTest extends FunSpec with Matchers {

  private val clock = fixed()
  private val maxAge = ofSeconds(10)
  private val timings = DefaultCacheTimings(MaxAgeTtl(maxAge), StaleIfErrorTtl(ofSeconds(2000)), StaleWhenRevalidateTtl(ofSeconds(3000)))

  describe("request") {

    describe("Adds If-Modified-Since") {
      it("works") {
        val maxAge = Duration.ofSeconds(1)
        result(Caching.Request.AddIfModifiedSince(clock, maxAge)(Request(),
          Service.mk { req => Future.value(req.headerMap.getAll("If-Modified-Since").mkString(",")) })
        ) shouldBe RFC_1123_DATE_TIME.format(ZonedDateTime.now(clock).minus(maxAge))
      }
    }
  }

  describe("response") {

    describe("FallbackCacheControl") {
      def getResponseWith(cacheTimings: DefaultCacheTimings, response: Response) = Await.result(FallbackCacheControl(clock, cacheTimings)(Request(), Service.mk((r) => Future.value(response))))

      it("adds the headers if they are not set") {
        val responseWithNoHeaders = Response(Status.Ok)
        val response = getResponseWith(timings, responseWithNoHeaders)
        headerOf("Cache-Control")(response) should be("public, max-age=10, stale-while-revalidate=3000, stale-if-error=2000")
        headerOf("Expires")(response) should be(RFC_1123_DATE_TIME.format(ZonedDateTime.now(clock).plus(maxAge)))
        headerOf("Vary")(response) should be("Accept-Encoding")
      }

      it("does not overwrite the headers if they are set") {
        val responseWithHeaders = Response(Status.Ok)
        responseWithHeaders.headerMap("Cache-Control") = "bob"
        responseWithHeaders.headerMap("Expires") = "rita"
        responseWithHeaders.headerMap("Vary") = "sue"
        val response = getResponseWith(timings, responseWithHeaders)
        headerOf("Cache-Control")(response) should be("bob")
        headerOf("Expires")(response) should be("rita")
        headerOf("Vary")(response) should be("sue")
      }
    }

    describe("NoCache") {
      it("does not cache non-GET requests") {
        val r = responseWith(Caching.Response.NoCache(), Request(Method.Put, ""))
        r.headerMap.isEmpty shouldBe true
      }

      it("adds correct headers to GET responses") {
        val r = responseWith(Caching.Response.NoCache(), Request(Method.Get, ""))
        headerOf("Expires")(r) shouldBe "0"
        headerOf("Cache-Control")(r) shouldBe "private, must-revalidate"
      }

      it("does not add headers if response fails predicate") {
        val r = responseWith(Caching.Response.NoCache(_ => false), Request(Method.Get, ""))
        r.headerMap.isEmpty shouldBe true
      }
    }

    describe("MaxAge") {
      it("does not cache non-GET requests") {
        val r = responseWith(Caching.Response.MaxAge(clock, Duration.ofHours(1)), Request(Method.Put, ""))
        r.headerMap.isEmpty shouldBe true
      }

      it("does not add headers if predicate fails") {
        val r = responseWith(Caching.Response.MaxAge(
          clock, Duration.ofHours(1), _ => false), Request(Method.Put, ""))
        r.headerMap.isEmpty shouldBe true
      }

      it("adds correct headers to GET requests based on current time") {
        val r = responseWith(Caching.Response.MaxAge(clock, Duration.ofHours(1)), Request(Method.Get, ""))
        headerOf("Expires")(r) shouldBe ZonedDateTime.now(clock).plusHours(1).format(RFC_1123_DATE_TIME)
        headerOf("Cache-Control")(r) shouldBe "public, max-age=3600"
      }
    }

    describe("Add eTag") {
      it("adds when predicate succeeds") {
        val response = Response()
        response.contentString = "bob"
        result(Caching.Response.AddETag[Request](_ => true)(Request(), Service.mk { req => Future.value(response) })).headerMap("ETag") shouldBe "9f9d51bc70ef21ca5c14f307980a29d8"
      }

      it("does not add when predicate fails") {
        val response = Response()
        response.contentString = "bob"
        result(Caching.Response.AddETag[Request](_ => false)(Request(), Service.mk { req => Future.value(response) })).headerMap.get("ETag") shouldBe None
      }
    }
  }

  def responseWith(caching: Filter[Request, Response, Request, Response], request: Request) = result(caching(request, Service.mk((r) => Future.value(Response()))))
}

