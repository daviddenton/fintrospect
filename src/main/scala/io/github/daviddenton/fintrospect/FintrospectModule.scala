package io.github.daviddenton.fintrospect

import io.github.daviddenton.fintrospect.util.ResponseBuilder
import ResponseBuilder._
import com.twitter.finagle.http.path.{Path, _}
import com.twitter.finagle.http.service.RoutingService
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Filter, Service, SimpleFilter}
import com.twitter.util.Future
import io.github.daviddenton.fintrospect.FintrospectModule._
import io.github.daviddenton.fintrospect.parameters.{PathParameter, Requirement}
import io.github.daviddenton.fintrospect.util.ArgoUtil.pretty
import org.jboss.netty.handler.codec.http.HttpMethod
import org.jboss.netty.handler.codec.http.HttpMethod.GET
import org.jboss.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST

object FintrospectModule {
  val IDENTIFY_SVC_HEADER = "descriptionServiceId"

  private type Svc = Service[Request, Response]
  private type Binding = PartialFunction[(HttpMethod, Path), Svc]
  private type PP[T] = PathParameter[T]

  def toService(binding: Binding): Svc = RoutingService.byMethodAndPathObject(binding)

  /**
   * Create a module using the given base-path and description renderer.
   */
  def apply(basePath: Path, renderer: Renderer): FintrospectModule = new FintrospectModule(basePath, renderer, Nil, PartialFunction.empty[(HttpMethod, Path), Svc])

  private case class ValidateParams(moduleRoute: ModuleRoute) extends SimpleFilter[Request, Response]() {
    override def apply(request: Request, service: Service[Request, Response]): Future[Response] = {
      val missingParams = moduleRoute.description.params.filter(_.requirement == Requirement.Mandatory).map(p => p.unapply(request).map(_ => None).getOrElse(Some(s"${p.name} (${p.paramType.name})"))).flatten
      if (missingParams.isEmpty) service(request) else Error(BAD_REQUEST, "Missing required parameters: " + missingParams.mkString(","))
    }
  }

  private case class Identify(moduleRoute: ModuleRoute) extends SimpleFilter[Request, Response]() {
    override def apply(request: Request, service: Service[Request, Response]): Future[Response] = {
      val url = if (moduleRoute.toString().length == 0) "/" else moduleRoute.toString()
      request.headers().set(IDENTIFY_SVC_HEADER, request.getMethod() + "." + url)
      service(request)
    }
  }

  private case class RoutesContent(descriptionContent: String) extends Service[Request, Response]() {
    override def apply(request: Request): Future[Response] = Ok(descriptionContent)
  }
}

/**
 * Self-describing module builder (uses the immutable builder pattern).
 */
class FintrospectModule private(basePath: Path, renderer: Renderer, moduleRoutes: List[ModuleRoute], private val userRoutes: Binding) {

  private def withDefault() = {
    withRoute(Description("Description route"), On(GET, identity), () => RoutesContent(pretty(renderer(moduleRoutes))))
  }

  private def withDescribedRoute(description: Description, on: On, PP: PP[_]*)(bindFn: Filter[Request, Response, Request, Response] => Binding): FintrospectModule = {
    val moduleRoute = new ModuleRoute(description, on, basePath, PP)
    new FintrospectModule(basePath, renderer, moduleRoute :: moduleRoutes,
      userRoutes.orElse(bindFn(ValidateParams(moduleRoute).andThen(Identify(moduleRoute)))))
  }

  /**
   * Calls back to the Route to attach itself to the Module.
   */
  def withRoute(route: Route): FintrospectModule = route.attachTo(this)

  /**
   * Attach described Route to the module.
   */
  def withRoute(description: Description, on: On, fn: () => Svc) = withDescribedRoute(description, on) {
    filtered => {
      case method -> path if on.matches(method, basePath, path) => filtered.andThen(fn())
    }
  }

  /**
   * Attach described Route to the module.
   */
  def withRoute[A](description: Description, on: On, PP0: PP[A], fn: A => Svc) = withDescribedRoute(description, on, PP0) {
    filtered => {
      case method -> path / PP0(s0) if on.matches(method, basePath, path) => filtered.andThen(fn(s0))
    }
  }

  /**
   * Attach described Route to the module.
   */
  def withRoute[A, B](description: Description, on: On, PP0: PP[A], PP1: PP[B], fn: (A, B) => Svc) = withDescribedRoute(description, on, PP0, PP1) {
    filtered => {
      case method -> path / PP0(s0) / PP1(s1) if on.matches(method, basePath, path) => filtered.andThen(fn(s0, s1))
    }
  }

  /**
   * Attach described Route to the module.
   */
  def withRoute[A, B, C](description: Description, on: On, PP0: PP[A], PP1: PP[B], PP2: PP[C], fn: (A, B, C) => Svc) = withDescribedRoute(description, on, PP0, PP1, PP2) {
    filtered => {
      case method -> path / PP0(s0) / PP1(s1) / PP2(s2) if on.matches(method, basePath, path) => filtered.andThen(fn(s0, s1, s2))
    }
  }

  /**
   * Attach described Route to the module.
   */
  def withRoute[A, B, C, D](description: Description, on: On, PP0: PP[A], PP1: PP[B], PP2: PP[C], PP3: PP[D], fn: (A, B, C, D) => Svc) = withDescribedRoute(description, on, PP0, PP1, PP2, PP3) {
    filtered => {
      case method -> path / PP0(s0) / PP1(s1) / PP2(s2) / PP3(s3) if on.matches(method, basePath, path) => filtered.andThen(fn(s0, s1, s2, s3))
    }
  }

  /**
   * Attach described Route to the module.
   */
  def withRoute[A, B, C, D, E](description: Description, on: On, PP0: PP[A], PP1: PP[B], PP2: PP[C], PP3: PP[D], PP4: PP[E], fn: (A, B, C, D, E) => Svc) = withDescribedRoute(description, on, PP0, PP1, PP2, PP3, PP4) {
    filtered => {
      case method -> path / PP0(s0) / PP1(s1) / PP2(s2) / PP3(s3) / PP4(s4) if on.matches(method, basePath, path) => filtered.andThen(fn(s0, s1, s2, s3, s4))
    }
  }

  /**
   * Attach described Route to the module.
   */
  def withRoute[A, B, C, D, E, F](description: Description, on: On, PP0: PP[A], PP1: PP[B], PP2: PP[C], PP3: PP[D], PP4: PP[E], PP5: PP[F], fn: (A, B, C, D, E, F) => Svc) = withDescribedRoute(description, on, PP0, PP1, PP2, PP3, PP4, PP5) {
    filtered => {
      case method -> path / PP0(s0) / PP1(s1) / PP2(s2) / PP3(s3) / PP4(s4) / PP5(s5) if on.matches(method, basePath, path) => filtered.andThen(fn(s0, s1, s2, s3, s4, s5))
    }
  }

  /**
   * Finaliser for the module builder to convert itself to a Partial Function which matches incoming requests.
   * Use this function when combining many modules together in an app.
   */
  def routes = withDefault().userRoutes

  /**
   * Finaliser for the module builder to convert itself to a Finagle Service. Use this function when there is only one module.
   */
  def toService = FintrospectModule.toService(routes)
}
