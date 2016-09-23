package io.fintrospect

import com.twitter.finagle.Service
import com.twitter.finagle.http.path.Path
import com.twitter.finagle.http.{Method, Request, Response}
import com.twitter.util.Future
import io.fintrospect.Module.ServiceBinding
import io.fintrospect.formats.{AbstractResponseBuilder, Argo}
import io.fintrospect.renderers.JsonErrorResponseRenderer

object Module {

  type ServiceBinding = PartialFunction[(Method, Path), Service[Request, Response]]

  /**
    * Combines many modules
    */
  def combine(modules: Module*): ServiceBinding = modules.map(_.serviceBinding).reduce(_.orElse(_))

  /**
    * Convert a ServiceBinding to a Finagle Service
    */
  def toService(binding: ServiceBinding, responseBuilder: AbstractResponseBuilder[_] = Argo.ResponseBuilder): Service[Request, Response] = {

    def pathFrom(req: Request) = {
      val u = req.uri
      u.indexOf('?') match {
        case -1 => u
        case n => u.substring(0, n)
      }
    }

    val routes: ServiceBinding = {
      case a if binding.isDefinedAt(a) => binding(a)
    }
    val notFoundPf: ServiceBinding = {
      case _ => Service.mk { r => Future.value(JsonErrorResponseRenderer.notFound()) }
    }

    Service.mk { request => (routes orElse notFoundPf) ((request.method, Path(pathFrom(request))))(request) }
  }
}

trait Module {

  /**
    * Finaliser for the module builder to combine itself with another module into a Partial Function which matches incoming requests.
    */
  def combine(that: Module): ServiceBinding = serviceBinding.orElse(that.serviceBinding)

  /**
    * Finaliser for the module builder to convert itself to a Finagle Service. Use this function when there is only one module.
    */
  def toService: Service[Request, Response] = Module.toService(serviceBinding)

  protected[fintrospect] def serviceBinding: ServiceBinding
}
