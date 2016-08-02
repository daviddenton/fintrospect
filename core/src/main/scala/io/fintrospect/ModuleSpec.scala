package io.fintrospect

import com.twitter.finagle.http.Method.Get
import com.twitter.finagle.http.Status.{BadRequest, NotFound}
import com.twitter.finagle.http.path.Path
import com.twitter.finagle.http.{Method, Request, Response}
import com.twitter.finagle.{Filter, Service}
import com.twitter.util.Future
import io.fintrospect.Module.ServiceBinding
import io.fintrospect.ModuleSpec.ModifyPath
import io.fintrospect.renderers.ModuleRenderer
import io.fintrospect.util.{ExtractionError, ExtractionFailed}

import scala.PartialFunction.empty

object ModuleSpec {
  type ModifyPath = Path => Path

  /**
    * Create a module using the given base-path without any Module API Rendering.
    */
  def apply(basePath: Path): ModuleSpec[Request, Response] = apply(basePath, new ModuleRenderer {
    override def description(basePath: Path, security: Security, routes: Seq[ServerRoute[_, _]]): Response = Response(NotFound)

    override def badRequest(badParameters: Seq[ExtractionError]): Response = Response(BadRequest)

    override def notFound(request: Request): Response = Response(NotFound)
  })

  /**
    * Create a module using the given base-path, renderer.
    */
  def apply(basePath: Path, moduleRenderer: ModuleRenderer): ModuleSpec[Request, Response] = {
    new ModuleSpec[Request, Response](basePath, moduleRenderer, identity, Nil, NoSecurity, Filter.identity)
  }

  /**
    * Create a module using the given base-path, renderer and module filter (to be applied to all matching requests to
    * this module APART from the documentation route).
    */
  def apply[RQ, RS](basePath: Path, moduleRenderer: ModuleRenderer, moduleFilter: Filter[Request, Response, RQ, RS]): ModuleSpec[RQ, RS] = {
    new ModuleSpec[RQ, RS](basePath, moduleRenderer, identity, Nil, NoSecurity, moduleFilter)
  }
}


/**
  * Self-describing module builder (uses the immutable builder pattern).
  */
class ModuleSpec[RQ, RS] private(basePath: Path,
                                 moduleRenderer: ModuleRenderer,
                                 descriptionRoutePath: ModifyPath,
                                 routes: Seq[ServerRoute[RQ, RS]],
                                 security: Security,
                                 moduleFilter: Filter[Request, Response, RQ, RS]) extends Module {

  private def validationFilter(route: ServerRoute[RQ, RS]) = Filter.mk[Request, Response, Request, Response] {
    (request, svc) =>
      route.routeSpec <--? request match {
        case ExtractionFailed(invalid) => Future.value(moduleRenderer.badRequest(invalid))
        case _ => svc(request)
      }
  }

  override protected[fintrospect] def serviceBinding: ServiceBinding =
    withDefault(routes.foldLeft(empty[(Method, Path), Service[Request, Response]]) {
      (currentBinding, route) =>
        val filter = identify(route).andThen(security.filter).andThen(validationFilter(route)).andThen(moduleFilter)
        currentBinding.orElse(route.toPf(filter, basePath))
    })

  /**
    * Set the API security for this module. This is implemented though a Filter which is invoked before the
    * parameter validation takes place, and will return Unauthorized HTTP response codes when a request does
    * not pass authentication.
    */
  def securedBy(newSecurity: Security): ModuleSpec[RQ, RS] =
    new ModuleSpec[RQ, RS](basePath, moduleRenderer, descriptionRoutePath, routes, newSecurity, moduleFilter)

  /**
    * Override the path from the root of this module (incoming) where the default module description will live.
    */
  def withDescriptionPath(newDefaultRoutePath: ModifyPath): ModuleSpec[RQ, RS] =
    new ModuleSpec[RQ, RS](basePath, moduleRenderer, newDefaultRoutePath, routes, security, moduleFilter)

  /**
    * Attach described Route(s) to the module. Request matching is attempted in the same order as in which this method is called.
    */
  def withRoute(newRoutes: ServerRoute[RQ, RS]*): ModuleSpec[RQ, RS] = new ModuleSpec(basePath, moduleRenderer, descriptionRoutePath, routes ++ newRoutes, security, moduleFilter)

  /**
    * Attach described Route(s) to the module. Request matching is attempted in the same order as in which this method is called.
    */
  def withRoutes(newRoutes: Iterable[ServerRoute[RQ, RS]]*): ModuleSpec[RQ, RS] = newRoutes.flatten.foldLeft(this)(_.withRoute(_))

  private def withDefault(otherRoutes: ServiceBinding): ServiceBinding = {
    val descriptionRoute = new UnboundRoute0(RouteSpec("Description route"), Get, descriptionRoutePath) bindTo {
      Service.mk { r: Request => Future.value(moduleRenderer.description(basePath, security, routes)) }
    }

    val fallback: ServiceBinding = {
      case _ => Service.mk { request: Request => Future.value(moduleRenderer.notFound(request)) }
    }

    val totalPf = otherRoutes.orElse(descriptionRoute.toPf(identify(descriptionRoute), basePath)).orElse(fallback)

    {
      case (method, path) if path.startsWith(basePath) => totalPf.apply((method, path))
    }
  }

  private def identify(route: ServerRoute[_, _]) = Filter.mk[Request, Response, Request, Response] {
    (request, svc) => {
      val url = if (route.describeFor(basePath).length == 0) "/" else route.describeFor(basePath)
      request.headerMap(Headers.IDENTIFY_SVC_HEADER) = request.method + ":" + url
      svc(request)
    }
  }
}
