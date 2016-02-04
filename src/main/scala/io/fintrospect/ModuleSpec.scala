package io.fintrospect

import com.twitter.finagle.http.Method._
import com.twitter.finagle.http.Status._
import com.twitter.finagle.http.path.Path
import com.twitter.finagle.http.{Method, Request, Response}
import com.twitter.finagle.{Filter, Service}
import com.twitter.util.Future
import io.fintrospect.Headers._
import io.fintrospect.ModuleSpec._
import io.fintrospect.Types.ServiceBinding
import io.fintrospect.parameters.{NoSecurity, Parameter, Security}
import io.fintrospect.renderers.ModuleRenderer

import scala.PartialFunction._

object ModuleSpec {
  type ModifyPath = Path => Path

  /**
    * Create a module using the given base-path without any Module API Renderering.
    */
  def apply(basePath: Path): ModuleSpec[Response] = apply(basePath, new ModuleRenderer {
    override def description(basePath: Path, security: Security, routes: Seq[ServerRoute[_]]): Response = Response(NotFound)

    override def badRequest(badParameters: Seq[Parameter]): Response = Response(BadRequest)

    override def notFound(request: Request): Response = Response(NotFound)
  })

  /**
    * Create a module using the given base-path, renderer.
    */
  def apply(basePath: Path, moduleRenderer: ModuleRenderer): ModuleSpec[Response] = {
    new ModuleSpec[Response](basePath, moduleRenderer, identity, Nil, NoSecurity, Filter.identity)
  }

  /**
    * Create a module using the given base-path, renderer and module filter (to be applied to all matching requests to
    * this module APART from the documentation route).
    */
  def apply[RS](basePath: Path, moduleRenderer: ModuleRenderer, moduleFilter: Filter[Request, Response, Request, RS]): ModuleSpec[RS] = {
    new ModuleSpec[RS](basePath, moduleRenderer, identity, Nil, NoSecurity, moduleFilter)
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
                             moduleFilter: Filter[Request, Response, Request, RS]) extends Module {
  override protected def serviceBinding: ServiceBinding = {
    withDefault(routes.foldLeft(empty[(Method, Path), Service[Request, Response]]) {
      (currentBinding, route) =>
        val filters = identify(route) +: security.filter +: validateParams(route) +: Nil
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

  /**
    * Attach described Route(s) to the module. Request matching is attempted in the same order as in which this method is called.
    */
  def withRoute(newRoutes: ServerRoute[RS]*): ModuleSpec[RS] = new ModuleSpec(basePath, moduleRenderer, descriptionRoutePath, routes ++ newRoutes, security, moduleFilter)

  /**
    * Attach described Route(s) to the module. Request matching is attempted in the same order as in which this method is called.
    */
  def withRoutes(newRoutes: Iterable[ServerRoute[RS]]*): ModuleSpec[RS] = newRoutes.flatten.foldLeft(this)(_.withRoute(_))

  private def withDefault(otherRoutes: ServiceBinding): ServiceBinding = {
    val descriptionRoute = new IncompletePath0(RouteSpec("Description route"), Get, descriptionRoutePath).bindTo {
      () => Service.mk { r => Future.value(moduleRenderer.description(basePath, security, routes)) }
    }

    val fallback: ServiceBinding = { case _ => Service.mk { request:Request => Future.value(moduleRenderer.notFound(request)) }}

    val totalPf = otherRoutes.orElse(descriptionRoute.toPf(Filter.identity, basePath)(identify(descriptionRoute))).orElse(fallback)

    {
      case (method, path) if path.startsWith(basePath) => totalPf.apply((method, path))
    }
  }

  private def validateParams(serverRoute: ServerRoute[_]) = Filter.mk[Request, Response, Request, Response] {
    (request, svc) => {
      val missingOrFailed = serverRoute.missingOrFailedFrom(request)
      if (missingOrFailed.isEmpty) svc(request) else Future.value(moduleRenderer.badRequest(missingOrFailed))
    }
  }

  private def identify(route: ServerRoute[_]) = Filter.mk[Request, Response, Request, Response] {
    (request, svc) => {
      val url = if (route.describeFor(basePath).length == 0) "/" else route.describeFor(basePath)
      request.headerMap.set(IDENTIFY_SVC_HEADER, request.method + ":" + url)
      svc(request)
    }
  }
}
