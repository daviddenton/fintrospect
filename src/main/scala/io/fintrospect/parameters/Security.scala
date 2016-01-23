package io.fintrospect.parameters

import com.twitter.finagle.http.Status._
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Service, SimpleFilter, Filter => FinFilter}
import com.twitter.util.Future
import io.fintrospect.Aliases._
import io.fintrospect.parameters.ApiKey.ValidateKey

import scala.util.{Failure, Success, Try}

/**
  * Endpoint security. Provides filter to be applied to endpoints for all requests.
  */
sealed trait Security {
  val filter: Filter[Response]
}

/**
  * Checks the presence of the named Api Key parameter. Filter returns 401 if Api-Key is not found in request.
  */
case class ApiKey[T, K >: Request](param: Parameter with Mandatory[T, K], validateKey: ValidateKey[T]) extends Security {
  val filter = new SimpleFilter[Request, Response] {
    override def apply(request: Request, next: Service[Request, Response]) =
      Try(param <-- request) match {
        case Success(apiKey) =>
          validateKey(apiKey)
            .flatMap(result => {
              if (result) next(request) else Future.value(Response(Unauthorized))
            })
        case Failure(e) => Future.value(Response(Unauthorized))
      }
  }
}

object ApiKey {
  type ValidateKey[T] = T => Future[Boolean]
}

/**
  * Default NoOp security filter. Filter allows all traffic through.
  */
object NoSecurity extends Security {
  val filter = FinFilter.identity[Request, Response]
}
