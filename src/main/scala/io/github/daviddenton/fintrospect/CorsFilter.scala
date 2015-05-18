package io.github.daviddenton.fintrospect

import com.twitter.finagle.http.filter.Cors.Policy
import com.twitter.util.Future
import io.github.daviddenton.fintrospect.FinagleTypeAliases.{FTFilter, FTRequest, FTResponse, FTService}
import io.github.daviddenton.fintrospect.util.ResponseBuilder._
import org.jboss.netty.handler.codec.http.HttpMethod

/**
 * This implementation is portef from the Finagle version, in order to add support for the Request type used by
 * Fintrospect. See:
 *
 */

class CorsFilter(policy: Policy) extends FTFilter {

  private def getOrigin(request: FTRequest): Option[String] = Option(request.headers.get("Origin")) flatMap { origin => policy.allowsOrigin(origin)}

  private def setOriginAndCredentials(response: FTResponse, origin: String): FTResponse = {
    response.headers.add("Access-Control-Allow-Origin", origin)
    if (policy.supportsCredentials && origin != "*") {
      response.headers.add("Access-Control-Allow-Credentials", "true")
    }
    response
  }

  private def setVary(response: FTResponse): FTResponse = {
    response.headers.set("Vary", "Origin")
    response
  }

  private def addExposedHeaders(response: FTResponse): FTResponse = {
    if (policy.exposedHeaders.nonEmpty) {
      response.headers.add(
        "Access-Control-Expose-Headers", policy.exposedHeaders.mkString(", "))
    }
    response
  }

  private def handleSimple(request: FTRequest, response: FTResponse): FTResponse =
    getOrigin(request) map {
      setOriginAndCredentials(response, _)
    } map {
      addExposedHeaders
    } getOrElse response

  private object Preflight {
    def unapply(request: FTRequest): Boolean =
      request.getMethod == HttpMethod.OPTIONS
  }

  private def getMethod(request: FTRequest): Option[String] =
    Option(request.headers.get("Access-Control-Request-Method"))

  private def setMethod(response: FTResponse, methods: Seq[String]): FTResponse = {
    response.headers.set("Access-Control-Allow-Methods", methods.mkString(", "))
    response
  }

  private def setMaxAge(response: FTResponse): FTResponse = {
    policy.maxAge foreach { maxAge =>
      response.headers.add("Access-Control-Max-Age", maxAge.inSeconds.toString)
    }
    response
  }

  private def getHeaders(request: FTRequest): Seq[String] =
    Option(request.headers.get("Access-Control-Request-Headers")) map {
      ", *".r.split(_).toSeq
    } getOrElse List.empty[String]

  private def setHeaders(response: FTResponse, headers: Seq[String]): FTResponse = {
    if (headers.nonEmpty) {
      response.headers.set("Access-Control-Allow-Headers", headers.mkString(", "))
    }
    response
  }

  /** http://www.w3.org/TR/cors/#resource-preflight-requests */
  private def handlePreflight(request: FTRequest): Option[FTResponse] =
    getOrigin(request) flatMap { origin =>
      getMethod(request) flatMap { method =>
        val headers = getHeaders(request)
        policy.allowsMethods(method) flatMap { allowedMethods =>
          policy.allowsHeaders(headers) map { allowedHeaders =>
            setHeaders(setMethod(setMaxAge(setOriginAndCredentials(Ok.build, origin)), allowedMethods), allowedHeaders)
          }
        }
      }
    }

  def apply(request: FTRequest, service: FTService): Future[FTResponse] = {
    val response = request match {
      case Preflight() => Future {
        handlePreflight(request) getOrElse Ok.build
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
