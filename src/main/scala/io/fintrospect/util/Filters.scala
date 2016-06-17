package io.fintrospect.util

import java.nio.charset.StandardCharsets.ISO_8859_1
import java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME
import java.time.{Clock, Duration, ZonedDateTime}
import java.util.Base64

import com.twitter.finagle.Filter
import com.twitter.finagle.http.Status.NotAcceptable
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.util.Future
import io.fintrospect.ContentType.fromAcceptHeaders
import io.fintrospect.configuration.{Authority, Credentials}
import io.fintrospect.formats.AbstractResponseBuilder
import io.fintrospect.formats.json.Argo
import io.fintrospect.formats.json.Argo.ResponseBuilder.implicits._
import io.fintrospect.parameters.{Extracted, Extraction, ExtractionFailed, Extractor, NotProvided}
import io.fintrospect.renderers.ModuleRenderer
import io.fintrospect.renderers.simplejson.SimpleJson
import io.fintrospect.{ContentType, ContentTypes, Headers}
import org.jboss.netty.handler.codec.http.HttpHeaders.Names.{ACCEPT, AUTHORIZATION, DATE, HOST}

/**
  * General case useful filters
  */
object Filters {

  /**
    * These filters operate on Requests (pre-flight)
    */
  object Request {

    /**
      * Intercept the request before it is sent to the next service.
      */
    def Tap(fn: Request => Unit) = Filter.mk[Request, Response, Request, Response] {
      (req, svc) => {
        fn(req)
        svc(req)
      }
    }

    /**
      * Print details of the request before it is sent to the next service. Useful for debugging.
      */
    val Print = Tap {
      req: Request => {
        println(s"***** REQUEST: ${req.uri} *****")
        println("Headers: " + req.headerMap)
        println("Params: " + req.params)
        println(s"Content (${req.contentString.length}b):" + req.contentString)
      }
    }

    /**
      * Respond with NotAcceptable unless: 1. No accept header, 2. Wildcard accept header, 3. Exact matching passed accept header
      */
    def StrictAccept(contentTypes: ContentType*) = Filter.mk[Request, Response, Request, Response] {
      (req, svc) => {
        fromAcceptHeaders(req)
          .filter(acceptable =>
            !acceptable.exists(contentTypes.contains) && !acceptable.contains(ContentTypes.WILDCARD)
          )
          .map(_ => NotAcceptable().toFuture)
          .getOrElse(svc(req))
      }
    }

    /**
      * Add Accept header to the Request.
      */
    def AddAccept[T](contentTypes: ContentType*) = Filter.mk[Request, T, Request, T] {
      (req, svc) => {
        contentTypes.foreach(c => req.headerMap.add(ACCEPT, c.value))
        svc(req)
      }
    }

    /**
      * Add Host header to the Request. This is mandatory in HTTP 1.1
      */
    def AddHost[T](authority: Authority) = Filter.mk[Request, T, Request, T] {
      (req, svc) => {
        req.headerMap(HOST) = authority.toString
        svc(req)
      }
    }

    /**
      * Add Authorization header with base-64 encoded credentials to the Request
      */
    def BasicAuthorization[T](credentials: Credentials) = Filter.mk[Request, T, Request, T] {
      (req, svc) => {
        val base64Credentials = Base64.getEncoder.encodeToString(s"${credentials.username}:${credentials.password}".getBytes(ISO_8859_1))
        req.headerMap(AUTHORIZATION) = "Basic " + base64Credentials.trim
        svc(req)
      }
    }

    /**
      * Extracts the input objects and feeds them into the underlying service.
      */
    def ExtractingRequest[I](fn: Request => Extraction[I])
                            (implicit moduleRenderer: ModuleRenderer = SimpleJson()):
    Filter[Request, Response, I, Response] = ExtractableRequest(Extractor.mk[Request, I](fn))(moduleRenderer)

    /**
      * Extracts the input objects and feeds them into the underlying service.
      */
    def ExtractableRequest[I](extractable: Extractor[Request, I])
                             (implicit moduleRenderer: ModuleRenderer = SimpleJson()):
    Filter[Request, Response, I, Response] = Filter.mk[Request, Response, I, Response] {
      (req, svc) => {
        extractable <--? req match {
          case Extracted(x) => svc(x)
          case NotProvided => Future.value(moduleRenderer.badRequest(Seq()))
          case ExtractionFailed(invalid) => Future.value(moduleRenderer.badRequest(invalid))
        }
      }
    }
  }

  /**
    * These filters operate on Responses (post-flight)
    */
  object Response {

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
      * Print details of the response before it is returned. Useful for debugging.
      */
    val Print = Tap {
      response: Response => {
        println(s"***** RESPONSE ${response.status.code} *****")
        println("Headers: " + response.headerMap)
        println(s"Content (${response.contentString.length}b):" + response.contentString)
      }
    }.andThen(TapFailure {
      t: Throwable => {
        println(s"***** RESPONSE FAILED *****")
        t.printStackTrace()
      }
    })

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
      * for identification. This is useful for logging metrics.
      */
    def ReportingRouteLatency(clock: Clock)(recordFn: (String, Duration) => Unit) = Filter.mk[Request, Response, Request, Response] {
      (req, svc) => {
        val start = clock.instant()

        svc(req).onSuccess {
          resp => {
            val identifier = List(
              req.headerMap.get(Headers.IDENTIFY_SVC_HEADER)
                .map(_.replace('.', '_').replace(':', '.'))
                .getOrElse(req.method.toString() + ".UNMAPPED")
                .replace('/', '_'),
              resp.status.code / 100 + "xx",
              resp.status.code.toString).mkString(".")

            recordFn(identifier, Duration.between(start, clock.instant()))
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

  /**
    * Print details of a request and it's response. Useful for debugging.
    */
  val PrintRequestAndResponse = Request.Print.andThen(Response.Print)
}
