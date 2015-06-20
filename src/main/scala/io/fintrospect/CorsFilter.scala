package io.fintrospect

import com.twitter.finagle.http.Response
import com.twitter.finagle.http.filter.Cors.Policy
import com.twitter.finagle.{Filter, Service}
import com.twitter.util.Future
import org.jboss.netty.handler.codec.http.{HttpMethod, HttpRequest, HttpResponse, HttpResponseStatus}

/**
 * This implementation is ported from the Finagle version, in order to add support for the Request type used by
 * Fintrospect. See:
 *
 */

class CorsFilter(policy: Policy) extends Filter[HttpRequest, HttpResponse, HttpRequest, HttpResponse] {

  private def getOrigin(request: HttpRequest): Option[String] = Option(request.headers.get("Origin")) flatMap { origin => policy.allowsOrigin(origin)}

  private def setOriginAndCredentials(response: HttpResponse, origin: String): HttpResponse = {
    response.headers.add("Access-Control-Allow-Origin", origin)
    if (policy.supportsCredentials && origin != "*") {
      response.headers.add("Access-Control-Allow-Credentials", "true")
    }
    response
  }

  private def setVary(response: HttpResponse): HttpResponse = {
    response.headers.set("Vary", "Origin")
    response
  }

  private def addExposedHeaders(response: HttpResponse): HttpResponse = {
    if (policy.exposedHeaders.nonEmpty) {
      response.headers.add(
        "Access-Control-Expose-Headers", policy.exposedHeaders.mkString(", "))
    }
    response
  }

  private def handleSimple(request: HttpRequest, response: HttpResponse): HttpResponse =
    getOrigin(request) map {
      setOriginAndCredentials(response, _)
    } map {
      addExposedHeaders
    } getOrElse response

  private object Preflight {
    def unapply(request: HttpRequest): Boolean =
      request.getMethod == HttpMethod.OPTIONS
  }

  private def getMethod(request: HttpRequest): Option[String] =
    Option(request.headers.get("Access-Control-Request-Method"))

  private def setMethod(response: HttpResponse, methods: Seq[String]): HttpResponse = {
    response.headers.set("Access-Control-Allow-Methods", methods.mkString(", "))
    response
  }

  private def setMaxAge(response: HttpResponse): HttpResponse = {
    policy.maxAge foreach { maxAge =>
      response.headers.add("Access-Control-Max-Age", maxAge.inSeconds.toString)
    }
    response
  }

  private def getHeaders(request: HttpRequest): Seq[String] =
    Option(request.headers.get("Access-Control-Request-Headers")) map {
      ", *".r.split(_).toSeq
    } getOrElse List.empty[String]

  private def setHeaders(response: HttpResponse, headers: Seq[String]): HttpResponse = {
    if (headers.nonEmpty) {
      response.headers.set("Access-Control-Allow-Headers", headers.mkString(", "))
    }
    response
  }

  /** http://www.w3.org/TR/cors/#resource-preflight-requests */
  private def handlePreflight(request: HttpRequest): Option[HttpResponse] =
    getOrigin(request) flatMap { origin =>
      getMethod(request) flatMap { method =>
        val headers = getHeaders(request)
        policy.allowsMethods(method) flatMap { allowedMethods =>
          policy.allowsHeaders(headers) map { allowedHeaders =>
            setHeaders(setMethod(setMaxAge(setOriginAndCredentials(Response(HttpResponseStatus.OK), origin)), allowedMethods), allowedHeaders)
          }
        }
      }
    }

  def apply(request: HttpRequest, service: Service[HttpRequest, HttpResponse]): Future[HttpResponse] = {
    val response = request match {
      case Preflight() => Future {
        handlePreflight(request) getOrElse Response(HttpResponseStatus.OK)
      }
      case _ => service(request) map {
        handleSimple(request, _)
      }
    }
    response map {
      setVary
    }
  }
}
