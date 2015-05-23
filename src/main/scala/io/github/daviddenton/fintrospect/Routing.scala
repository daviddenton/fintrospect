package io.github.daviddenton.fintrospect

import com.twitter.finagle.Service
import com.twitter.finagle.http.path.Path
import com.twitter.util.Future
import io.github.daviddenton.fintrospect.util.ResponseBuilder._
import org.jboss.netty.handler.codec.http.HttpResponseStatus._
import org.jboss.netty.handler.codec.http.{HttpMethod, HttpRequest, HttpResponse}

class Routing private(routes: PartialFunction[HttpRequest, Service[HttpRequest, HttpResponse]]) extends Service[HttpRequest, HttpResponse] {
  private val notFoundPf: PartialFunction[HttpRequest, Service[HttpRequest, HttpResponse]] = {
    case _ => new Service[HttpRequest, HttpResponse] {
      def apply(request: HttpRequest): Future[HttpResponse] = Json.Error(NOT_FOUND, "No such route")
    }
  }
  private val requestToService = routes orElse notFoundPf

  def apply(request: HttpRequest): Future[HttpResponse] = requestToService(request)(request)
}

object Routing {

  private[fintrospect] type Binding = PartialFunction[(HttpMethod, Path), Service[HttpRequest, HttpResponse]]

  def fromBinding(binding: Binding) =
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
