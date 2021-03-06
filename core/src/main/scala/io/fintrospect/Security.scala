package io.fintrospect

import com.twitter.finagle.http.Status.Unauthorized
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Filter, Service}
import com.twitter.util.Future
import io.fintrospect.parameters.{Mandatory, Parameter}

import scala.util.{Failure, Success, Try}

/**
  * Endpoint security. Provides filter to be applied to endpoints for all requests.
  */
sealed trait Security {
  val filter: Filter[Request, Response, Request, Response]
}

/**
  * Checks the presence of the named Api Key parameter. Filter returns 401 if Api-Key is not found in request.
  */
case class ApiKey[T, K >: Request](param: Parameter with Mandatory[K, T], validateKey: Service[T, Boolean]) extends Security {
  val filter = Filter.mk[Request, Response, Request, Response] {
    (request, svc) =>
      Try(param <-- request) match {
        case Success(apiKey) => validateKey(apiKey)
          .flatMap(result => if (result) svc(request) else Future(Response(Unauthorized)))
        case Failure(_) => Future(Response(Unauthorized))
      }
  }
}

/**
  * Default NoOp security filter. Filter allows all traffic through.
  */
object NoSecurity extends Security {
  val filter = Filter.identity[Request, Response]
}
