package io.github.daviddenton.fintrospect

import com.twitter.finagle.http.path.Path
import com.twitter.finagle.http.service.RoutingService
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Filter, Service, SimpleFilter}
import com.twitter.util.Future
import io.github.daviddenton.fintrospect.FintrospectModule2._
import io.github.daviddenton.fintrospect.parameters.{PathParameter, Requirement}
import io.github.daviddenton.fintrospect.util.ArgoUtil.pretty
import io.github.daviddenton.fintrospect.util.ResponseBuilder._
import org.jboss.netty.handler.codec.http.HttpMethod
import org.jboss.netty.handler.codec.http.HttpMethod.GET
import org.jboss.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST

object FintrospectModule2 {
  val IDENTIFY_SVC_HEADER = "descriptionServiceId"

  type FF = Filter[Request, Response, Request, Response]
  type Svc = Service[Request, Response]
  type Binding = PartialFunction[(HttpMethod, Path), Svc]
  private type PP[T] = PathParameter[T]

  def toService(binding: Binding): Svc = RoutingService.byMethodAndPathObject(binding)

  /**
   * Create a module using the given base-path and description renderer.
   */
  def apply(basePath: Path, renderer: Renderer): FintrospectModule2 = new FintrospectModule2(basePath, renderer, Nil, PartialFunction.empty[(HttpMethod, Path), Svc])

  private case class ValidateParams(moduleRoute: ModuleRoute2) extends SimpleFilter[Request, Response]() {
    override def apply(request: Request, service: Service[Request, Response]): Future[Response] = {
      val missingParams = moduleRoute.description.params.filter(_.requirement == Requirement.Mandatory).map(p => p.unapply(request).map(_ => None).getOrElse(Some(s"${p.name} (${p.paramType.name})"))).flatten
      if (missingParams.isEmpty) service(request) else Error(BAD_REQUEST, "Missing required parameters: " + missingParams.mkString(","))
    }
  }

  private case class Identify(moduleRoute: ModuleRoute2) extends SimpleFilter[Request, Response]() {
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
class FintrospectModule2 private(basePath: Path, renderer: Renderer, moduleRoutes: List[ModuleRoute2], private val userRoutes: Binding) {

  private def withDefault() = withRoute(Description("Description route"), PathWrapper(GET).then(() => RoutesContent(pretty(null))))

  /**
   * Calls back to the Route to attach itself to the Module.
   */
  def withRoute(route: Route2): FintrospectModule2 = route.attachTo(this)

  /**
   * Attach described Route to the module.
   */
  def withRoute(description: Description, completePath: CompletePath): FintrospectModule2 = {
    val moduleRoute = new ModuleRoute2(description, completePath, basePath)
    new FintrospectModule2(basePath, renderer, moduleRoute :: moduleRoutes,
      userRoutes.orElse(completePath.toPf(basePath)(ValidateParams(moduleRoute).andThen(Identify(moduleRoute)))))
  }

  /**
   * Finaliser for the module builder to convert itself to a Partial Function which matches incoming requests.
   * Use this function when combining many modules together in an app.
   */
  def routes = withDefault().userRoutes

  /**
   * Finaliser for the module builder to convert itself to a Finagle Service. Use this function when there is only one module.
   */
  def toService = FintrospectModule2.toService(routes)
}
