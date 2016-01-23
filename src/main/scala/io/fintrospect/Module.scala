package io.fintrospect

import com.twitter.finagle.Service
import com.twitter.finagle.http.Status._
import com.twitter.finagle.http.path.Path
import com.twitter.finagle.http.{Request, Response}
import io.fintrospect.Types.ServiceBinding
import io.fintrospect.formats.json.Argo.ResponseBuilder._

object Module {
  /**
    * Combines many modules
    */
  def combine(modules: Module*): ServiceBinding = modules.map(_.serviceBinding).reduce(_.orElse(_))

  /**
    * Convert a ServiceBinding to a Finagle Service
    */
  def toService(binding: ServiceBinding): Service[Request, Response] = {
    def pathFrom(req: Request) = {
      val u = req.uri
      u.indexOf('?') match {
        case -1 => u
        case n => u.substring(0, n)
      }
    }

    val routes = new PartialFunction[Request, Service[Request, Response]] {
      def apply(request: Request) = {
        binding((request.method, Path(pathFrom(request))))
      }

      def isDefinedAt(request: Request) = binding.isDefinedAt((request.method, Path(pathFrom(request))))
    }

    val notFoundPf: PartialFunction[Request, Service[Request, Response]] = {
      case _ => Service.mk { r => NotFound("No route found on this path. Have you used the correct HTTP verb?") }
    }

    val requestToService = routes orElse notFoundPf

    Service.mk {
      request => {
        requestToService(request)(request)
      }
    }
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

    protected def serviceBinding: ServiceBinding
  }
