package io.fintrospect.filters

import java.security.MessageDigest
import java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME
import java.time.{Clock, Duration, ZonedDateTime}

import com.twitter.finagle.http.{Method, Request, Response}
import com.twitter.finagle.{Filter, Service, SimpleFilter}
import com.twitter.util.Future
import org.jboss.netty.handler.codec.http.HttpHeaders.Names.{CACHE_CONTROL, DATE, ETAG, EXPIRES, IF_MODIFIED_SINCE, VARY}

/**
  * Useful filters for applying Cache-Controls to request/responses
  */
object Caching {

  case class DefaultCacheTimings(maxAge: MaxAgeTtl,
                                 staleIfErrorTtl: StaleIfErrorTtl,
                                 staleWhenRevalidateTtl: StaleWhenRevalidateTtl)

  sealed abstract class CacheControlHeaderPart(name: String, value: Duration) {
    def toHeaderValue: String = if (value.getSeconds > 0) s"$name=${value.getSeconds}" else ""
  }

  case class StaleWhenRevalidateTtl(value: Duration) extends CacheControlHeaderPart("stale-while-revalidate", value)

  case class StaleIfErrorTtl(value: Duration) extends CacheControlHeaderPart("stale-if-error", value)

  case class MaxAgeTtl(value: Duration) extends CacheControlHeaderPart("max-age", value)

  /**
    * These filters operate on Requests (pre-flight)
    */
  object Request {
    def AddIfModifiedSince[T](clock: Clock, maxAge: Duration) = Filter.mk[Request, T, Request, T] {
      (req, svc) => {
        req.headerMap(IF_MODIFIED_SINCE) = RFC_1123_DATE_TIME.format(ZonedDateTime.now(clock).minus(maxAge))
        svc(req)
      }
    }
  }

  /**
    * These filters operate on Responses (post-flight)
    */
  object Response {

    private abstract class CacheFilter(predicate: Response => Boolean) extends SimpleFilter[Request, Response] {
      def headersFor(response: Response): Map[String, String]

      override def apply(request: Request, next: Service[Request, Response]): Future[Response] = next(request).map {
        response => {
          val headers = if (request.method == Method.Get && predicate(response)) headersFor(response) else Map()
          for ((key, value) <- headers) {
            response.headerMap(key) = value
          }
          response
        }
      }
    }

    /**
      * By default, only applies when the status code of the response is < 400. This is overridable and useful -
      * For example you could combine this with a MaxAge for everything >= 400
      */
    def NoCache(predicate: Response => Boolean = _.statusCode < 400): Filter[Request, Response, Request, Response] = new CacheFilter(predicate) {
      override def headersFor(response: Response) = Map(CACHE_CONTROL -> "private, must-revalidate", EXPIRES -> "0")
    }

    /**
      * By default, only applies when the status code of the response is < 400. This is overridable.
      */
    def MaxAge(clock: Clock, maxAge: Duration, predicate: Response => Boolean = _.statusCode < 400): Filter[Request, Response, Request, Response] = new CacheFilter(predicate) {
      override def headersFor(response: Response) = Map(
        CACHE_CONTROL -> Seq("public", MaxAgeTtl(maxAge).toHeaderValue).mkString(", "),
        EXPIRES -> RFC_1123_DATE_TIME.format(now(response).plusSeconds(maxAge.getSeconds)))

      private def now(response: Response): ZonedDateTime = {
        try {
          response.headerMap.get(DATE)
            .map(RFC_1123_DATE_TIME.parse)
            .map(ZonedDateTime.from)
            .getOrElse(ZonedDateTime.now(clock))
        } catch {
          case e: Exception => ZonedDateTime.now(clock)
        }
      }
    }

    /**
      * Hash algo stolen from http://stackoverflow.com/questions/26423662/scalatra-response-hmac-calulation
      * By default, only applies when the status code of the response is < 400. This is overridable.
      */
    def AddETag[T](predicate: Response => Boolean = _.statusCode < 400): Filter[T, Response, T, Response] = Filter.mk[T, Response, T, Response] {
      (req, svc) => svc(req)
        .map {
          rsp => {
            if(predicate(rsp)) {
              val hashedBody = MessageDigest.getInstance("MD5").digest(rsp.contentString.getBytes).map("%02x".format(_)).mkString
              rsp.headerMap(ETAG) = hashedBody
            }
            rsp
          }
        }
    }

    /**
      * Applies the passed cache timings (Cache-Control, Expires, Vary) to responses, but only if they are not there already.
      * Use this for adding default cache settings.
      * By default, only applies when the status code of the response is < 400. This is overridable.
      */
    def FallbackCacheControl(clock: Clock, defaultCacheTimings: DefaultCacheTimings, predicate: Response => Boolean = _.statusCode < 400) = new SimpleFilter[Request, Response] {
      override def apply(request: Request, next: Service[Request, Response]): Future[Response] = next(request).map {
        response => request.method match {
          case Method.Get if predicate(response) => addDefaultCacheHeadersIfAbsent(response)
          case _ => response
        }
      }

      private def addDefaultHeaderIfAbsent(response: Response, header: String, defaultProducer: => String) {
        response.headerMap(header) = response.headerMap.getOrElse(header, defaultProducer)
      }

      private def addDefaultCacheHeadersIfAbsent(response: Response): Response = {
        addDefaultHeaderIfAbsent(response, CACHE_CONTROL,
          Seq("public", defaultCacheTimings.maxAge.toHeaderValue, defaultCacheTimings.staleWhenRevalidateTtl.toHeaderValue, defaultCacheTimings.staleIfErrorTtl.toHeaderValue).mkString(", "))
        addDefaultHeaderIfAbsent(response, EXPIRES, RFC_1123_DATE_TIME.format(ZonedDateTime.now(clock).plus(defaultCacheTimings.maxAge.value)))
        addDefaultHeaderIfAbsent(response, VARY, "Accept-Encoding")
        response
      }
    }
  }


}
