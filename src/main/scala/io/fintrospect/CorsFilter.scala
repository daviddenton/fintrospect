package io.fintrospect

import com.twitter.finagle.httpx.filter.Cors.Policy
import com.twitter.finagle.httpx.{Method, Request, Response, Status}
import com.twitter.finagle.{Filter, Service}
import com.twitter.util.Future

/**
 * This implementation is ported from the Finagle version, in order to add support for the Request type used by
 * Fintrospect.
 *
 */

class CorsFilter(policy: Policy) extends Filter[Request, Response, Request, Response] {

  private def getOrigin(request: Request): Option[String] = request.headerMap.get("Origin") flatMap { origin => policy.allowsOrigin(origin)}

  private def setOriginAndCredentials(response: Response, origin: String): Response = {
    response.headerMap.add("Access-Control-Allow-Origin", origin)
    if (policy.supportsCredentials && origin != "*") {
      response.headerMap.add("Access-Control-Allow-Credentials", "true")
    }
    response
  }

  private def setVary(response: Response): Response = {
    response.headerMap.set("Vary", "Origin")
    response
  }

  private def addExposedHeaders(response: Response): Response = {
    if (policy.exposedHeaders.nonEmpty) {
      response.headerMap.add(
        "Access-Control-Expose-Headers", policy.exposedHeaders.mkString(", "))
    }
    response
  }

  private def handleSimple(request: Request, response: Response): Response =
    getOrigin(request) map {
      setOriginAndCredentials(response, _)
    } map {
      addExposedHeaders
    } getOrElse response

  private object Preflight {
    def unapply(request: Request): Boolean =
      request.method == Method.Options
  }

  private def getMethod(request: Request): Option[String] =
    request.headerMap.get("Access-Control-Request-Method")

  private def setMethod(response: Response, methods: Seq[String]): Response = {
    response.headerMap.set("Access-Control-Allow-Methods", methods.mkString(", "))
    response
  }

  private def setMaxAge(response: Response): Response = {
    policy.maxAge foreach { maxAge =>
      response.headerMap.add("Access-Control-Max-Age", maxAge.inSeconds.toString)
    }
    response
  }

  private def getHeaders(request: Request): Seq[String] =
    request.headerMap.get("Access-Control-Request-Headers") map {
      ", *".r.split(_).toSeq
    } getOrElse Seq.empty[String]

  private def setHeaders(response: Response, headers: Seq[String]): Response = {
    if (headers.nonEmpty) {
      response.headerMap.set("Access-Control-Allow-Headers", headers.mkString(", "))
    }
    response
  }

  /** http://www.w3.org/TR/cors/#resource-preflight-requests */
  private def handlePreflight(request: Request): Option[Response] =
    getOrigin(request) flatMap { origin =>
      getMethod(request) flatMap { method =>
        val headers = getHeaders(request)
        policy.allowsMethods(method) flatMap { allowedMethods =>
          policy.allowsHeaders(headers) map { allowedHeaders =>
            setHeaders(setMethod(setMaxAge(setOriginAndCredentials(Response(Status.Ok), origin)), allowedMethods), allowedHeaders)
          }
        }
      }
    }

  def apply(request: Request, service: Service[Request, Response]): Future[Response] = {
    val response = request match {
      case Preflight() => Future {
        handlePreflight(request) getOrElse Response(Status.Ok)
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
