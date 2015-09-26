package io.fintrospect

import com.twitter.finagle.Service
import com.twitter.finagle.http.path.Path
import com.twitter.util.Future
import io.fintrospect.util.ResponseBuilder._
import io.fintrospect.util.json.Argo
import org.jboss.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND
import org.jboss.netty.handler.codec.http.{HttpMethod, HttpRequest, HttpResponse}

class Routing private(routes: PartialFunction[HttpRequest, Service[HttpRequest, HttpResponse]]) extends Service[HttpRequest, HttpResponse] {
  private val notFoundPf: PartialFunction[HttpRequest, Service[HttpRequest, HttpResponse]] = {
    case _ => new Service[HttpRequest, HttpResponse] {
      def apply(request: HttpRequest): Future[HttpResponse] = Argo.ResponseBuilder.Error(NOT_FOUND, "No route found on this path. Have you used the correct HTTP verb?")
    }
  }
  private val requestToService = routes orElse notFoundPf

  def apply(request: HttpRequest): Future[HttpResponse] = requestToService(request)(request)
}

object Routing {

  private[fintrospect] type RouteBinding = PartialFunction[(HttpMethod, Path), Service[HttpRequest, HttpResponse]]

  def fromBinding(binding: RouteBinding) =
    new Routing(
      new PartialFunction[HttpRequest, Service[HttpRequest, HttpResponse]] {
        def apply(request: HttpRequest) = {
          binding((request.getMethod, Path(pathFrom(request))))
        }

        def isDefinedAt(request: HttpRequest) = binding.isDefinedAt((request.getMethod, Path(pathFrom(request))))
      })

  private def pathFrom(req: HttpRequest) = {
    val u = req.getUri
    u.indexOf('?') match {
      case -1 => u
      case n => u.substring(0, n)
    }
  }
}
