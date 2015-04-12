package io.github.daviddenton.fintrospect

import com.twitter.finagle.http.path.Path
import com.twitter.finagle.http.service.RoutingService
import com.twitter.finagle.SimpleFilter
import com.twitter.util.Future
import io.github.daviddenton.fintrospect.FintrospectModule._
import io.github.daviddenton.fintrospect.FintrospectTypes._
import io.github.daviddenton.fintrospect.parameters.Requirement
import io.github.daviddenton.fintrospect.util.ArgoUtil.pretty
import io.github.daviddenton.fintrospect.util.ResponseBuilder._
import org.jboss.netty.handler.codec.http.HttpMethod
import org.jboss.netty.handler.codec.http.HttpMethod.GET
import org.jboss.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST

object FintrospectModule {
  val IDENTIFY_SVC_HEADER = "descriptionServiceId"

  def toService(binding: PF): Service = RoutingService.byMethodAndPathObject(binding)

  /**
   * Create a module using the given base-path and description renderer.
   */
  def apply(basePath: Path, renderer: Renderer): FintrospectModule = new FintrospectModule(basePath, renderer, Nil, PartialFunction.empty[(HttpMethod, Path), Service])

  private case class ValidateParams(route: Route) extends SimpleFilter[Request, Response]() {
    override def apply(request: Request, service: Service): Future[Response] = {
      val missingParams = route.description.params.filter(_.requirement == Requirement.Mandatory).map(p => p.unapply(request).map(_ => None).getOrElse(Some(s"${p.name} (${p.paramType.name})"))).flatten
      if (missingParams.isEmpty) service(request) else Error(BAD_REQUEST, "Missing required parameters: " + missingParams.mkString(","))
    }
  }

  private case class Identify(route: Route, basePath: Path) extends SimpleFilter[Request, Response]() {
    override def apply(request: Request, service: Service): Future[Response] = {
      val url = if (route.describeFor(basePath).length == 0) "/" else route.describeFor(basePath)
      request.headers().set(IDENTIFY_SVC_HEADER, request.getMethod() + "." + url)
      service(request)
    }
  }

  private case class RoutesContent(descriptionContent: String) extends Service() {
    override def apply(request: Request): Future[Response] = Ok(descriptionContent)
  }

}

/**
 * Self-describing module builder (uses the immutable builder pattern).
 */
class FintrospectModule private(basePath: Path, renderer: Renderer, theRoutes: List[Route], private val binding: PartialFunction[(HttpMethod, Path), Service]) {

  private def withDefault() = withRoute(DescribedRoute("Description route").at(GET).then(() => RoutesContent(pretty(renderer(basePath, theRoutes)))))

  /**
   * Attach described Route to the module.
   */
  def withRoute(route: Route): FintrospectModule = {
    new FintrospectModule(basePath, renderer, route :: theRoutes,
      binding.orElse(route.toPf(basePath)(ValidateParams(route).andThen(Identify(route, basePath)))))
  }

  /**
   * Finaliser for the module builder to convert itself to a Partial Function which matches incoming requests.
   * Use this function when combining many modules together in an app.
   */
  def routes: PartialFunction[(HttpMethod, Path), Service] = withDefault().binding

  /**
   * Finaliser for the module builder to convert itself to a Finagle Service. Use this function when there is only one module.
   */
  def toService: Service = FintrospectModule.toService(routes)
}
