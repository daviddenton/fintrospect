package io.fintrospect.util
import java.security.MessageDigest
import java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME
import java.time.{Clock, Duration, ZonedDateTime}

import com.twitter.finagle.http.{Method, Request, Response}
import com.twitter.finagle.{Filter, Service, SimpleFilter}
import com.twitter.util.Future

object Caching {

  case class DefaultCacheTimings(maxAge: MaxAgeTtl,
                                 staleIfErrorTtl: StaleIfErrorTtl,
                                 staleWhenRevalidateTtl: StaleWhenRevalidateTtl)

  sealed abstract class CacheControlHeaderPart(name: String, value: Duration) {
    def toHeaderValue: String = if(value.getSeconds > 0) s"$name=${value.getSeconds}" else ""
  }

  case class StaleWhenRevalidateTtl(value: Duration) extends CacheControlHeaderPart("stale-while-revalidate", value)
  case class StaleIfErrorTtl(value: Duration) extends CacheControlHeaderPart("stale-if-error", value)
  case class MaxAgeTtl(value: Duration) extends CacheControlHeaderPart("max-age", value)

  private abstract class CacheFilter extends SimpleFilter[Request, Response] {
    def headersFor(response: Response): Map[String, String]

    override def apply(request: Request, next: Service[Request, Response]): Future[Response] = next(request).map {
      response => {
        val headers = if (request.method == Method.Get) headersFor(response) else Map()
        for ((key, value) <- headers) {
          response.headerMap(key) = value
        }
        response
      }
    }
  }

  def NoCache(): SimpleFilter[Request, Response] = new CacheFilter {
    override def headersFor(response: Response) = Map("Cache-Control" -> "private, must-revalidate", "Expires" -> "0")
  }

  def MaxAge(clock: Clock, maxAge: Duration): SimpleFilter[Request, Response] = new CacheFilter {
    override def headersFor(response: Response) = Map(
      "Cache-Control" -> Seq("public", new MaxAgeTtl(maxAge).toHeaderValue).mkString(", "),
      "Expires" -> RFC_1123_DATE_TIME.format(now(response).plusSeconds(maxAge.getSeconds)))

    private def now(response: Response): ZonedDateTime = {
      try {
        response.headerMap.get("Date")
          .map(RFC_1123_DATE_TIME.parse)
          .map(ZonedDateTime.from)
          .getOrElse(ZonedDateTime.now(clock))
      } catch {
        case e: Exception => ZonedDateTime.now(clock)
      }
    }
  }

  // Stolen from http://stackoverflow.com/questions/26423662/scalatra-response-hmac-calulation
  def AddETag[T](): Filter[T, Response, T, Response] = Filter.mk[T, Response, T, Response] {
    (req, svc) => svc(req)
      .map {
        rsp => {
          val hashedBody = MessageDigest.getInstance("MD5").digest(rsp.contentString.getBytes).map("%02x".format(_)).mkString
          rsp.headerMap("ETag") = hashedBody
          rsp
        }
      }
  }

  /**
    * Applies the passed cache timings (Cache-Control, Expires, Vary) to responses, but only if they are not there already.
    * Use this for adding default cache settings.
    */
  def FallbackCacheControl(clock: Clock, defaultCacheTimings: DefaultCacheTimings) = new SimpleFilter[Request, Response] {
    override def apply(request: Request, next: Service[Request, Response]): Future[Response] = next(request).map {
      response => request.method match {
        case Method.Get => addDefaultCacheHeadersIfAbsent(response)
        case _ => response
      }
    }

    private def addDefaultHeaderIfAbsent(response: Response, header: String, defaultProducer: => String) {
      response.headerMap(header) = response.headerMap.getOrElse(header, defaultProducer)
    }

    private def addDefaultCacheHeadersIfAbsent(response: Response): Response = {
      addDefaultHeaderIfAbsent(response, "Cache-Control",
        Seq("public", defaultCacheTimings.maxAge.toHeaderValue, defaultCacheTimings.staleWhenRevalidateTtl.toHeaderValue, defaultCacheTimings.staleIfErrorTtl.toHeaderValue).mkString(", "))
      addDefaultHeaderIfAbsent(response, "Expires", RFC_1123_DATE_TIME.format(ZonedDateTime.now(clock).plus(defaultCacheTimings.maxAge.value)))
      addDefaultHeaderIfAbsent(response, "Vary", "Accept-Encoding")
      response
    }
  }

}
