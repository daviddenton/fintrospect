package io.fintrospect

import com.twitter.finagle.Service
import com.twitter.finagle.httpx._
import com.twitter.finagle.httpx.path.Path
import com.twitter.util.Future
import io.fintrospect.formats.ResponseBuilder._
import io.fintrospect.formats.json.Argo

class Routing private(routes: PartialFunction[Request, Service[Request, Response]]) extends Service[Request, Response] {
  private val notFoundPf: PartialFunction[Request, Service[Request, Response]] = {
    case _ => new Service[Request, Response] {
      def apply(request: Request): Future[Response] = Argo.ResponseBuilder.Error(Status.NotFound, "No route found on this path. Have you used the correct HTTP verb?")
    }
  }
  private val requestToService = routes orElse notFoundPf

  def apply(request: Request): Future[Response] = requestToService(request)(request)
}

object Routing {

  private[fintrospect] type RouteBinding = PartialFunction[(Method, Path), Service[Request, Response]]

  def fromBinding(binding: RouteBinding) =
    new Routing(
      new PartialFunction[Request, Service[Request, Response]] {
        def apply(request: Request) = {
          binding((request.method, Path(pathFrom(request))))
        }

        def isDefinedAt(request: Request) = binding.isDefinedAt((request.method, Path(pathFrom(request))))
      })

  private def pathFrom(req: Request) = {
    val u = req.uri
    u.indexOf('?') match {
      case -1 => u
      case n => u.substring(0, n)
    }
  }
}
