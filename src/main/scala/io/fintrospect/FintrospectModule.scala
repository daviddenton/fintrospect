package io.fintrospect

import com.twitter.finagle.http.path.Path
import com.twitter.finagle.{Filter, Service, SimpleFilter}
import com.twitter.util.Future
import io.fintrospect.FintrospectModule._
import io.fintrospect.Headers._
import io.fintrospect.Routing.fromBinding
import io.fintrospect.renderers.ModuleRenderer
import io.fintrospect.util.ResponseBuilder._
import org.jboss.netty.handler.codec.http.HttpMethod.GET
import org.jboss.netty.handler.codec.http.{HttpMethod, HttpRequest, HttpResponse}

import scala.PartialFunction._

object FintrospectModule {

  private type Binding = PartialFunction[(HttpMethod, Path), Service[HttpRequest, HttpResponse]]

  private type TFilter = Filter[HttpRequest, HttpResponse, HttpRequest, HttpResponse]

  /**
   * Combines many modules
   */
  def combine(modules: FintrospectModule*): Binding = modules.map(_.totalBinding).reduce(_.orElse(_))

  /**
   * Convert a Binding to a Finagle Service
   */
  def toService(binding: Binding): Service[HttpRequest, HttpResponse] = fromBinding(binding)

  /**
   * Create a module using the given base-path, renderer.
   */
  def apply(basePath: Path, moduleRenderer: ModuleRenderer): FintrospectModule = new FintrospectModule(basePath, moduleRenderer, identity, Nil, Filter.mk((in, svc) => svc(in)))

  /**
   * Create a module using the given base-path, renderer and module filter (to be applied to all matching requests to this module).
   */
  def apply(basePath: Path, moduleRenderer: ModuleRenderer, moduleFilter: Filter[HttpRequest, HttpResponse, HttpRequest, HttpResponse]): FintrospectModule = {
    new FintrospectModule(basePath, moduleRenderer, identity, Nil, moduleFilter)
  }

  private class ValidateParams(route: Route, moduleRenderer: ModuleRenderer) extends SimpleFilter[HttpRequest, HttpResponse]() {
    override def apply(request: HttpRequest, service: Service[HttpRequest, HttpResponse]): Future[HttpResponse] = {
      val missingOrFailed = route.missingOrFailedFrom(request)
      if (missingOrFailed.isEmpty) service(request) else moduleRenderer.badRequest(missingOrFailed)
    }
  }

  private class Identify(route: Route, basePath: Path) extends SimpleFilter[HttpRequest, HttpResponse]() {
    override def apply(request: HttpRequest, service: Service[HttpRequest, HttpResponse]): Future[HttpResponse] = {
      val url = if (route.describeFor(basePath).length == 0) "/" else route.describeFor(basePath)
      request.headers().set(IDENTIFY_SVC_HEADER, request.getMethod + "." + url)
      service(request)
    }
  }

}

/**
 * Self-describing module builder (uses the immutable builder pattern).
 */
class FintrospectModule private(basePath: Path,
                                moduleRenderer: ModuleRenderer,
                                descriptionRoutePath: Path => Path,
                                routes: Seq[Route],
                                moduleFilter: Filter[HttpRequest, HttpResponse, HttpRequest, HttpResponse]) {
  private def totalBinding = {
    withDefault(routes.foldLeft(empty[(HttpMethod, Path), Service[HttpRequest, HttpResponse]]) {
      (currentBinding, route) =>
        val filters = new Identify(route, basePath) +: new ValidateParams(route, moduleRenderer) +: moduleFilter +: Seq[TFilter]()
        currentBinding.orElse(route.toPf(basePath)(filters.reduce(_.andThen(_))))
    })
  }

  /**
   * Override the path from the root of this module (incoming) where the default module description will live.
   */
  def withDescriptionPath(newDefaultRoutePath: Path => Path): FintrospectModule = {
    new FintrospectModule(basePath, moduleRenderer, newDefaultRoutePath, routes, moduleFilter)
  }

  private def withDefault(otherRoutes: PartialFunction[(HttpMethod, Path), Service[HttpRequest, HttpResponse]]) = {
    val descriptionRoute = new IncompletePath0(DescribedRoute("Description route"), GET, descriptionRoutePath).bindTo(() => {
      Service.mk((req) => moduleRenderer.description(basePath, routes))
    })

    otherRoutes.orElse(descriptionRoute.toPf(basePath)(new Identify(descriptionRoute, basePath)))
  }

  /**
   * Attach described Route to the module.
   */
  def withRoute(route: Route): FintrospectModule = new FintrospectModule(basePath, moduleRenderer, descriptionRoutePath, route +: routes, moduleFilter)

  /**
   * Finaliser for the module builder to combine itself with another module into a Partial Function which matches incoming requests.
   */
  def combine(that: FintrospectModule): Binding = totalBinding.orElse(that.totalBinding)

  /**
   * Finaliser for the module builder to convert itself to a Finagle Service. Use this function when there is only one module.
   */
  def toService: Service[HttpRequest, HttpResponse] = FintrospectModule.toService(totalBinding)
}
