package io.fintrospect.filters

import java.nio.charset.StandardCharsets.ISO_8859_1
import java.util.Base64

import com.google.common.net.HttpHeaders
import com.twitter.finagle.Filter
import com.twitter.finagle.http.Status.NotAcceptable
import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.Future
import io.fintrospect.ContentType.fromAcceptHeaders
import io.fintrospect.configuration.{Authority, Credentials}
import io.fintrospect.formats.Argo.ResponseBuilder.implicits._
import io.fintrospect.renderers.ModuleRenderer
import io.fintrospect.renderers.simplejson.SimpleJson
import io.fintrospect.util.{Extracted, Extraction, ExtractionFailed, Extractor}
import io.fintrospect.{ContentType, ContentTypes}
import org.jboss.netty.handler.codec.http.HttpHeaders.Names.{ACCEPT, AUTHORIZATION, HOST}

/**
  * These filters operate on Requests (pre-flight)
  */
object RequestFilters {

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
    * Add User-Agent header to the Request.
    */
  def AddUserAgent[T](user: String) = Filter.mk[Request, T, Request, T] {
    (req, svc) => {
      req.headerMap(HttpHeaders.USER_AGENT) = user
      svc(req)
    }
  }

  /**
    * Add Authorization header with base-64 encoded credentials to the Request
    */
  def BasicAuthorization(credentials: Credentials) = Filter.mk[Request, Response, Request, Response] {
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
        case Extracted(Some(x)) => svc(x)
        case Extracted(None) => Future.value(moduleRenderer.badRequest(Nil))
        case ExtractionFailed(invalid) => Future.value(moduleRenderer.badRequest(invalid))
      }
    }
  }
}
