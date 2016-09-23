package io.fintrospect.filters

import java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME
import java.time.{Clock, Duration, ZonedDateTime}

import com.twitter.finagle.Filter
import com.twitter.finagle.http.{Request, Response, Status}
import io.fintrospect.Headers
import io.fintrospect.formats.{AbstractResponseBuilder, Argo}
import io.fintrospect.util.{Extraction, Extractor}
import org.jboss.netty.handler.codec.http.HttpHeaders.Names.DATE

/**
  * These filters operate on Responses (post-flight)
  */
object ResponseFilters {

  /**
    * Intercept a successful response before it is returned.
    */
  def Tap(rFn: Response => Unit) = Filter.mk[Request, Response, Request, Response] {
    (req, svc) => svc(req).onSuccess(rFn)
  }

  /**
    * Intercept a failed response before it is returned.
    */
  def TapFailure(t: Throwable => Unit) = Filter.mk[Request, Response, Request, Response] {
    (req, svc) => svc(req).onFailure(t)
  }

  /**
    * Add Date header to the Response in RFC1123 format.
    */
  def AddDate[T](clock: Clock = Clock.systemUTC()) = Filter.mk[T, Response, T, Response] {
    (req, svc) => {
      svc(req)
        .map(Response => {
          Response.headerMap(DATE) = RFC_1123_DATE_TIME.format(ZonedDateTime.now(clock))
          Response
        })
    }
  }

  /**
    * Report the latency on a particular route to a callback function, passing the "X-Fintrospect-Route-Name" header and response status bucket (e.g. 2xx)
    * for identification. This is useful for logging metrics. Note that the passed function blocks the response from completing.
    */
  def ReportingRouteLatency(clock: Clock)(recordFn: (String, Duration) => Unit) = Filter.mk[Request, Response, Request, Response] {
    (req, svc) => {
      val start = clock.instant()

      svc(req).map {
        resp => {
          val identifier = List(
            req.headerMap.get(Headers.IDENTIFY_SVC_HEADER)
              .map(_.replace('.', '_').replace(':', '.'))
              .getOrElse(req.method.toString() + ".UNMAPPED")
              .replace('/', '_'),
            resp.status.code / 100 + "xx",
            resp.status.code.toString).mkString(".")

          recordFn(identifier, Duration.between(start, clock.instant()))
          resp
        }
      }
    }
  }

  /**
    * Last-gasp Filter which converts uncaught exceptions and converts them into INTERNAL_SERVER_ERRORs
    */
  def CatchAll(responseBuilder: AbstractResponseBuilder[_] = Argo.ResponseBuilder) = Filter.mk[Request, Response, Request, Response] {
    (req, svc) => {
      svc(req).handle {
        case t: Throwable => responseBuilder.HttpResponse(Status.InternalServerError).withError(t).build()
      }
    }
  }

  /**
    * Extracts an object form the Response output object and feeds them into the underlying service.
    */
  def ExtractingResponse[O](fn: Response => Extraction[O]): Filter[Request, Extraction[O], Request, Response] =
    ExtractingResponse(Extractor.mk(fn))

  /**
    * Extracts the output objects and feeds them into the underlying service.
    */
  def ExtractingResponse[O](extractable: Extractor[Response, O]): Filter[Request, Extraction[O], Request, Response] =
    Filter.mk[Request, Extraction[O], Request, Response] {
      (req, svc) => svc(req).map(extractable.<--?)
    }
}
