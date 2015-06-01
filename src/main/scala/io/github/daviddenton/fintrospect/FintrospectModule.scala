package io.github.daviddenton.fintrospect

import com.twitter.finagle.http.path.Path
import com.twitter.finagle.{Filter, Service, SimpleFilter}
import com.twitter.util.Future
import io.github.daviddenton.fintrospect.FintrospectModule._
import io.github.daviddenton.fintrospect.Routing.fromBinding
import io.github.daviddenton.fintrospect.parameters.Requirement._
import io.github.daviddenton.fintrospect.renderers.ModuleRenderer
import io.github.daviddenton.fintrospect.util.ResponseBuilder._
import org.jboss.netty.handler.codec.http.HttpMethod.GET
import org.jboss.netty.handler.codec.http.{HttpMethod, HttpRequest, HttpResponse}

import scala.PartialFunction._
import scala.language.existentials

object FintrospectModule {

  private type Binding = PartialFunction[(HttpMethod, Path), Service[HttpRequest, HttpResponse]]

  private type TFilter = Filter[HttpRequest, HttpResponse, HttpRequest, HttpResponse]
  val IDENTIFY_SVC_HEADER = "X-Fintrospect-Route-Name"

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

  private case class ValidateParams(route: Route, moduleRenderer: ModuleRenderer) extends SimpleFilter[HttpRequest, HttpResponse]() {
    override def apply(request: HttpRequest, service: Service[HttpRequest, HttpResponse]): Future[HttpResponse] = {
      val paramsAndParseResults = route.describedRoute.params.map(p => (p, p.parseFrom(request)))
      val withoutMissingOptionalParams = paramsAndParseResults.filterNot(pr => pr._1.requirement == Optional && pr._2.isEmpty)
      val missingOrFailed = withoutMissingOptionalParams.filterNot(pr => pr._2.isDefined && pr._2.get.isSuccess).map(_._1)
      if (missingOrFailed.isEmpty) service(request) else moduleRenderer.badRequest(missingOrFailed)
    }
  }

  private case class Identify(route: Route, basePath: Path) extends SimpleFilter[HttpRequest, HttpResponse]() {
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
                                routes: List[Route],
                                moduleFilter: Filter[HttpRequest, HttpResponse, HttpRequest, HttpResponse]) {
  private def totalBinding = {
    withDefault(routes.foldLeft(empty[(HttpMethod, Path), Service[HttpRequest, HttpResponse]]) {
      (currentBinding, route) =>
        val filters = Identify(route, basePath) :: ValidateParams(route, moduleRenderer) :: moduleFilter :: List[TFilter]()
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
    val descriptionRoute = IncompletePath0(DescribedRoute("Description route"), GET, descriptionRoutePath).bindTo(() => {
      Service.mk((req) => moduleRenderer.description(basePath, routes))
    })

    otherRoutes.orElse(descriptionRoute.toPf(basePath)(Identify(descriptionRoute, basePath)))
  }

  /**
   * Attach described Route to the module.
   */
  def withRoute(route: Route): FintrospectModule = new FintrospectModule(basePath, moduleRenderer, descriptionRoutePath, route :: routes, moduleFilter)

  /**
   * Finaliser for the module builder to combine itself with another module into a Partial Function which matches incoming requests.
   */
  def combine(that: FintrospectModule): Binding = totalBinding.orElse(that.totalBinding)

  /**
   * Finaliser for the module builder to convert itself to a Finagle Service. Use this function when there is only one module.
   */
  def toService: Service[HttpRequest, HttpResponse] = FintrospectModule.toService(totalBinding)
}
