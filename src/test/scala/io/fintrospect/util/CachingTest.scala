package io.fintrospect.util

import java.time.Duration.ofSeconds
import java.time.format.DateTimeFormatter._
import java.time.{Clock, Duration, Instant, ZoneId, ZonedDateTime}

import com.twitter.finagle.http.{Method, Request, Response, Status}
import com.twitter.finagle.{Filter, Service}
import com.twitter.util.Await.result
import com.twitter.util.{Await, Future}
import io.fintrospect.util.Caching.{DefaultCacheTimings, MaxAgeTtl, StaleIfErrorTtl, StaleWhenRevalidateTtl}
import org.scalatest.{FunSpec, ShouldMatchers}

class CachingTest extends FunSpec with ShouldMatchers {

  private val clock = Clock.fixed(ZonedDateTime.now().toInstant, ZoneId.systemDefault())
  private val maxAge = ofSeconds(10)
  private val timings = DefaultCacheTimings(MaxAgeTtl(maxAge), StaleIfErrorTtl(ofSeconds(2000)), StaleWhenRevalidateTtl(ofSeconds(3000)))

  describe("FallbackCacheControl") {
    def getResponseWith(cacheTimings: DefaultCacheTimings, response: Response) = Await.result(Caching.FallbackCacheControl(clock, cacheTimings)(Request(), Service.mk((r) => Future.value(response))))

    it("adds the headers if they are not set") {
      val responseWithNoHeaders = Response(Status.Ok)
      val response = getResponseWith(timings, responseWithNoHeaders)
      response.headerMap("Cache-Control") should be("public, max-age=10, stale-while-revalidate=3000, stale-if-error=2000")
      response.headerMap("Expires") should be(RFC_1123_DATE_TIME.format(ZonedDateTime.now(clock).plus(maxAge)))
      response.headerMap("Vary") should be("Accept-Encoding")
    }

    it("does not overwrite the headers if they are set") {
      val responseWithHeaders = Response(Status.Ok)
      responseWithHeaders.headerMap("Cache-Control") = "bob"
      responseWithHeaders.headerMap("Expires") = "rita"
      responseWithHeaders.headerMap("Vary") = "sue"
      val response = getResponseWith(timings, responseWithHeaders)
      response.headerMap("Cache-Control") should be("bob")
      response.headerMap("Expires") should be("rita")
      response.headerMap("Vary") should be("sue")
    }
  }

  describe("NoCache") {
    it("does not cache non-GET requests") {
      val r = responseWith(Caching.NoCache(), Request(Method.Put, ""))
      r.headerMap.isEmpty shouldBe true
    }

    it("adds correct headers to GET requests") {
      val r = responseWith(Caching.NoCache(), Request(Method.Get, ""))
      r.headerMap("Expires") shouldBe "0"
      r.headerMap("Cache-Control") shouldBe "private, must-revalidate"
    }
  }

  describe("MaxAge") {
    it("does not cache non-GET requests") {
      val r = responseWith(Caching.MaxAge(Clock.fixed(Instant.now(), ZoneId.systemDefault()), Duration.ofHours(1)), Request(Method.Put, ""))
      r.headerMap.isEmpty shouldBe true
    }

    it("adds correct headers to GET requests based on current time") {
      val now = ZonedDateTime.now()
      val r = responseWith(Caching.MaxAge(Clock.fixed(now.toInstant, ZoneId.systemDefault()), Duration.ofHours(1)), Request(Method.Get, ""))
      r.headerMap("Expires") shouldBe now.plusHours(1).format(RFC_1123_DATE_TIME)
      r.headerMap("Cache-Control") shouldBe "public, max-age=3600"
    }
  }

  describe("Add eTag") {
    it("passes through when predicate succeeds") {
      val response: Response = Response()
      response.contentString = "bob"
      result(Caching.AddETag[Request]()(Request(), Service.mk { req => Future.value(response) })).headerMap("ETag") shouldBe "9f9d51bc70ef21ca5c14f307980a29d8"
    }
  }

  def responseWith(caching: Filter[Request, Response, Request, Response], request: Request) = result(caching(request, Service.mk((r) => Future.value(Response()))))
}

