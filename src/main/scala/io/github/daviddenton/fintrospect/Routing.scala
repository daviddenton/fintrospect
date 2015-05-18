package io.github.daviddenton.fintrospect

import com.twitter.finagle.http.path.Path
import com.twitter.util.Future
import io.github.daviddenton.fintrospect.FinagleTypeAliases._
import io.github.daviddenton.fintrospect.util.ResponseBuilder.Error
import org.jboss.netty.handler.codec.http.HttpResponseStatus._

class Routing private(routes: PartialFunction[FTRequest, FTService]) extends FTService {
  private val notFoundPf: PartialFunction[FTRequest, FTService] = {
    case _ => new FTService {
      def apply(request: FTRequest): Future[FTResponse] = Error(NOT_FOUND, "No such route")
    }
  }
  private val requestToService = routes orElse notFoundPf

  def apply(request: FTRequest): Future[FTResponse] = requestToService(request)(request)
}

object Routing {
  def fromBinding(binding: Binding) =
    new Routing(
      new PartialFunction[FTRequest, FTService] {
        def apply(request: FTRequest) = {
          binding((request.getMethod, Path(pathFrom(request))))
        }

        def isDefinedAt(request: FTRequest) = binding.isDefinedAt((request.getMethod, Path(pathFrom(request))))
      })

  private def pathFrom(req: FTRequest) = {
    val u = req.getUri
    u.indexOf('?') match {
      case -1 => u
      case n => u.substring(0, n)
    }
  }
}
