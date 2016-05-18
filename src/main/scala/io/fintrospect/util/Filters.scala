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
import io.fintrospect.parameters.{Extracted, Extraction, ExtractionFailed}
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

    def AddAccept[T](contentTypes: ContentType*) = Filter.mk[Request, T, Request, T] {
      (req, svc) => {
        contentTypes.foreach(c => req.headerMap.add(ACCEPT, c.value))
        svc(req)
      }
    }

    def AddHost[T](authority: Authority) = Filter.mk[Request, T, Request, T] {
      (req, svc) => {
        req.headerMap(HOST) = authority.toString
        svc(req)
      }
    }

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
    Filter[Request, Response, I, Response] = Filter.mk[Request, Response, I, Response] {
      (req, svc) => {
        fn(req) match {
          case Extracted(x) if x.isDefined => svc(x.get)
          case Extracted(x) if x.isEmpty => Future.value(moduleRenderer.badRequest(Seq()))
          case ExtractionFailed(invalid) => Future.value(moduleRenderer.badRequest(invalid))
        }
      }
    }

  }

  /**
    * These filters operate on Responses (post-flight)
    */
  object Response {

    def AddDate[T](clock: Clock = Clock.systemUTC()) = Filter.mk[T, Response, T, Response] {
      (req, svc) => {
        svc(req)
          .map(rsp => {
            rsp.headerMap(DATE) = RFC_1123_DATE_TIME.format(ZonedDateTime.now(clock))
            rsp
          })
      }
    }

    def ReportingRouteLatency(clock: Clock)(recordFn: (String, Duration) => Unit) = Filter.mk[Request, Response, Request, Response] {
      (req, svc) => {
        val start = clock.instant()
        for {
          resp <- svc(req)
        } yield {
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
  }

}
