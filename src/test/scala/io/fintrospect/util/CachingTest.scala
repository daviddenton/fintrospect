package io.fintrospect.util

import java.time.Duration.ofSeconds
import java.time.format.DateTimeFormatter._
import java.time.{Clock, Duration, Instant, ZoneId, ZonedDateTime}

import com.twitter.finagle.http.{Method, Request, Response, Status}
import com.twitter.finagle.{Filter, Service}
import com.twitter.util.Await.result
import com.twitter.util.{Await, Future}
import io.fintrospect.util.Caching.Response.FallbackCacheControl
import io.fintrospect.util.Caching.{DefaultCacheTimings, MaxAgeTtl, StaleIfErrorTtl, StaleWhenRevalidateTtl}
import io.fintrospect.util.HttpRequestResponseUtil.headerOf
import io.fintrospect.util.TestClocks.fixed
import org.scalatest.{FunSpec, ShouldMatchers}

class CachingTest extends FunSpec with ShouldMatchers {

  private val maxAge = ofSeconds(10)
  private val timings = DefaultCacheTimings(MaxAgeTtl(maxAge), StaleIfErrorTtl(ofSeconds(2000)), StaleWhenRevalidateTtl(ofSeconds(3000)))

  describe("request") {

    describe("Adds If-Modified-Since") {
      it("works") {
        val maxAge = Duration.ofSeconds(1)
        result(Caching.Request.AddIfModifiedSince(fixed, maxAge)(Request(),
          Service.mk { req => Future.value(req.headerMap.getAll("If-Modified-Since").mkString(",")) })
        ) shouldBe RFC_1123_DATE_TIME.format(ZonedDateTime.now(fixed).minus(maxAge))
      }
    }
  }

  describe("response") {

    describe("FallbackCacheControl") {
      def getResponseWith(cacheTimings: DefaultCacheTimings, response: Response) = Await.result(FallbackCacheControl(TestClocks.fixed, cacheTimings)(Request(), Service.mk((r) => Future.value(response))))

      it("adds the headers if they are not set") {
        val responseWithNoHeaders = Response(Status.Ok)
        val response = getResponseWith(timings, responseWithNoHeaders)
        headerOf("Cache-Control")(response) should be("public, max-age=10, stale-while-revalidate=3000, stale-if-error=2000")
        headerOf("Expires")(response) should be(RFC_1123_DATE_TIME.format(ZonedDateTime.now(TestClocks.fixed).plus(maxAge)))
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

      it("adds correct headers to GET requests") {
        val r = responseWith(Caching.Response.NoCache(), Request(Method.Get, ""))
        headerOf("Expires")(r) shouldBe "0"
        headerOf("Cache-Control")(r) shouldBe "private, must-revalidate"
      }
    }

    describe("MaxAge") {
      it("does not cache non-GET requests") {
        val r = responseWith(Caching.Response.MaxAge(Clock.fixed(Instant.now(), ZoneId.systemDefault()), Duration.ofHours(1)), Request(Method.Put, ""))
        r.headerMap.isEmpty shouldBe true
      }

      it("adds correct headers to GET requests based on current time") {
        val now = ZonedDateTime.now()
        val r = responseWith(Caching.Response.MaxAge(Clock.fixed(now.toInstant, ZoneId.systemDefault()), Duration.ofHours(1)), Request(Method.Get, ""))
        headerOf("Expires")(r) shouldBe now.plusHours(1).format(RFC_1123_DATE_TIME)
        headerOf("Cache-Control")(r) shouldBe "public, max-age=3600"
      }
    }

    describe("Add eTag") {
      it("passes through when predicate succeeds") {
        val response: Response = Response()
        response.contentString = "bob"
        result(Caching.Response.AddETag[Request]()(Request(), Service.mk { req => Future.value(response) })).headerMap("ETag") shouldBe "9f9d51bc70ef21ca5c14f307980a29d8"
      }
    }
  }


  def responseWith(caching: Filter[Request, Response, Request, Response], request: Request) = result(caching(request, Service.mk((r) => Future.value(Response()))))
}

