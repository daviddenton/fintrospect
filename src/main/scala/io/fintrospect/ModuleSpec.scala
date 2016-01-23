package io.fintrospect

import com.twitter.finagle.http.Method._
import com.twitter.finagle.http.Status.{BadRequest, NotFound}
import com.twitter.finagle.http.path.Path
import com.twitter.finagle.http.{Response, Method, Request}
import com.twitter.finagle.{Filter, Service, SimpleFilter}
import com.twitter.util.Future
import io.fintrospect.Headers._
import io.fintrospect.ModuleSpec._
import io.fintrospect.Routing.fromBinding
import io.fintrospect.parameters.{NoSecurity, Parameter, Security}
import io.fintrospect.renderers.ModuleRenderer

import scala.PartialFunction._


object ModuleSpec {
  type ModifyPath = Path => Path

  private type Binding = PartialFunction[(Method, Path), Service[Request, Response]]

  /**
    * Combines many modules
    */
  def combine(modules: ModuleSpec[_]*): Binding = modules.map(_.totalBinding).reduce(_.orElse(_))

  /**
    * Convert a Binding to a Finagle Service
    */
  def toService(binding: Binding): Service[Request, Response] = fromBinding(binding)

  /**
    * Create a module using the given base-path without any Module API Renderering.
    */
  def apply(basePath: Path): ModuleSpec[Response] = apply(basePath, new ModuleRenderer {
    override def description(basePath: Path, security: Security, routes: Seq[ServerRoute[_]]): Response = Response(NotFound)

    override def badRequest(badParameters: Seq[Parameter]): Response = Response(BadRequest)
  })

  /**
    * Create a module using the given base-path, renderer.
    */
  def apply(basePath: Path, moduleRenderer: ModuleRenderer): ModuleSpec[Response] = {
    val filter: Filter[Request, Response, Request, Response] = Filter.mk[Request, Response, Request, Response]((r, svc) => svc(r))
    new ModuleSpec[Response](basePath, moduleRenderer, identity, Nil, NoSecurity, filter)
  }

  /**
    * Create a module using the given base-path, renderer and module filter (to be applied to all matching requests to
    * this module APART from the documentation route).
    */
  def apply[RS](basePath: Path, moduleRenderer: ModuleRenderer, moduleFilter: Filter[Request, Response, Request, RS]): ModuleSpec[RS] = {
    new ModuleSpec[RS](basePath, moduleRenderer, identity, Nil, NoSecurity, moduleFilter)
  }

  private class ValidateParams(serverRoute: ServerRoute[_], moduleRenderer: ModuleRenderer) extends SimpleFilter[Request, Response]() {
    override def apply(request: Request, service: Service[Request, Response]): Future[Response] = {
      val missingOrFailed = serverRoute.missingOrFailedFrom(request)
      if (missingOrFailed.isEmpty) service(request) else Future.value(moduleRenderer.badRequest(missingOrFailed))
    }
  }

  private class Identify2(route: ServerRoute[_], basePath: Path) extends SimpleFilter[Request, Response]() {
    override def apply(request: Request, service: Service[Request, Response]): Future[Response] = {
      val url = if (route.describeFor(basePath).length == 0) "/" else route.describeFor(basePath)
      request.headerMap.set(IDENTIFY_SVC_HEADER, request.method + ":" + url)
      service(request)
    }
  }

}

/**
  * Self-describing module builder (uses the immutable builder pattern).
  */
class ModuleSpec[RS] private(basePath: Path,
                             moduleRenderer: ModuleRenderer,
                             descriptionRoutePath: ModifyPath,
                             routes: Seq[ServerRoute[RS]],
                             security: Security,
                             moduleFilter: Filter[Request, Response, Request, RS]) {
  private def totalBinding: PartialFunction[(Method, Path), Service[Request, Response]] = {
    withDefault(routes.foldLeft(empty[(Method, Path), Service[Request, Response]]) {
      (currentBinding, route) =>
        val filters = new Identify2(route, basePath) +: security.filter +: new ValidateParams(route, moduleRenderer) +: Nil
        currentBinding.orElse(route.toPf(moduleFilter, basePath)(filters.reduce(_.andThen(_))))
    })
  }

  /**
    * Set the API security for this module. This is implemented though a Filter which is invoked before the
    * parameter validation takes place, and will return Unauthorized HTTP response codes when a request does
    * not pass authentication.
    */
  def securedBy(newSecurity: Security): ModuleSpec[RS] = {
    new ModuleSpec[RS](basePath, moduleRenderer, descriptionRoutePath, routes, newSecurity, moduleFilter)
  }

  /**
    * Override the path from the root of this module (incoming) where the default module description will live.
    */
  def withDescriptionPath(newDefaultRoutePath: ModifyPath): ModuleSpec[RS] = {
    new ModuleSpec[RS](basePath, moduleRenderer, newDefaultRoutePath, routes, security, moduleFilter)
  }

  private def withDefault(otherRoutes: PartialFunction[(Method, Path), Service[Request, Response]]) = {
    val descriptionRoute = new IncompletePath0(RouteSpec("Description route"), Get, descriptionRoutePath).bindTo(() => new Service[Request, Response] {
      override def apply(request: Request) = Future.value(moduleRenderer.description(basePath, security, routes))
    })

    val default: Filter[Request, Response, Request, Response] = Filter.identity
    otherRoutes.orElse(descriptionRoute.toPf(default, basePath)(new Identify2(descriptionRoute, basePath)))
  }

  /**
    * Attach described Route(s) to the module. Request matching is attempted in the same order as in which this method is called.
    */
  def withRoute(newRoutes: ServerRoute[RS]*): ModuleSpec[RS] = new ModuleSpec(basePath, moduleRenderer, descriptionRoutePath, routes ++ newRoutes, security, moduleFilter)

  /**
    * Attach described Route(s) to the module. Request matching is attempted in the same order as in which this method is called.
    */
  def withRoutes(newRoutes: Iterable[ServerRoute[RS]]*): ModuleSpec[RS] = newRoutes.flatten.foldLeft(this)(_.withRoute(_))

  /**
    * Finaliser for the module builder to combine itself with another module into a Partial Function which matches incoming requests.
    */
  def combine(that: ModuleSpec[_]): Binding = totalBinding.orElse(that.totalBinding)

  /**
    * Finaliser for the module builder to convert itself to a Finagle Service. Use this function when there is only one module.
    */
  def toService: Service[Request, Response] = ModuleSpec.toService(totalBinding)
}
