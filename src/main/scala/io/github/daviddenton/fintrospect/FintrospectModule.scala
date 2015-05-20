package io.github.daviddenton.fintrospect

import com.twitter.finagle.http.path.Path
import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.util.Future
import io.github.daviddenton.fintrospect.FintrospectModule._
import io.github.daviddenton.fintrospect.Routing.fromBinding
import io.github.daviddenton.fintrospect.parameters.Requirement._
import io.github.daviddenton.fintrospect.util.ArgoUtil.pretty
import io.github.daviddenton.fintrospect.util.ResponseBuilder._
import org.jboss.netty.handler.codec.http.HttpMethod.GET
import org.jboss.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST
import org.jboss.netty.handler.codec.http.{HttpMethod, HttpRequest, HttpResponse}

import scala.PartialFunction._

object FintrospectModule {

  private type Binding = PartialFunction[(HttpMethod, Path), Service[HttpRequest, HttpResponse]]

  val IDENTIFY_SVC_HEADER = "descriptionServiceId"

  /**
   * Combines many modules
   */
  def combine(modules: FintrospectModule*): Binding = modules.map(_.totalBinding).reduce(_.orElse(_))

  /**
   * Convert a Binding to a Finagle Service
   */
  def toService(binding: Binding): Service[HttpRequest, HttpResponse] = fromBinding(binding)

  /**
   * Create a module using the given base-path and description renderer.
   */
  def apply(basePath: Path, renderer: Renderer): FintrospectModule = {
    new FintrospectModule(basePath, renderer, Nil, empty[(HttpMethod, Path), Service[HttpRequest, HttpResponse]])
  }

  private case class ValidateParams(route: Route) extends SimpleFilter[HttpRequest, HttpResponse]() {
    override def apply(request: HttpRequest, service: Service[HttpRequest, HttpResponse]): Future[HttpResponse] = {
      val allMandatoryParams = route.describedRoute.params.filter(_.requirement == Mandatory)
      val parsedParamResults = allMandatoryParams.map(p => (p, p.parseFrom(request)))
      val missingOrFailed = parsedParamResults.filterNot(pr => pr._2.isDefined && pr._2.get.isSuccess)
      val messages = missingOrFailed.map(p => Some(s"${p._1.name} (${p._1.paramType.name})"))
      if (messages.isEmpty) service(request) else Error(BAD_REQUEST, "Missing required parameters: " + messages.mkString(","))
    }
  }

  private case class Identify(route: Route, basePath: Path) extends SimpleFilter[HttpRequest, HttpResponse]() {
    override def apply(request: HttpRequest, service: Service[HttpRequest, HttpResponse]): Future[HttpResponse] = {
      val url = if (route.describeFor(basePath).length == 0) "/" else route.describeFor(basePath)
      request.headers().set(IDENTIFY_SVC_HEADER, request.getMethod + "." + url)
      service(request)
    }
  }

  private case class RoutesContent(descriptionContent: String) extends Service[HttpRequest, HttpResponse]() {
    override def apply(request: HttpRequest): Future[HttpResponse] = Ok(descriptionContent)
  }
}

/**
 * Self-describing module builder (uses the immutable builder pattern).
 */
class FintrospectModule private(basePath: Path, renderer: Renderer, theRoutes: List[Route], private val currentBinding: Binding) {

  private def withDefault() = withRoute(DescribedRoute("Description route").at(GET).bindTo(() => RoutesContent(pretty(renderer(basePath, theRoutes)))))

  private def totalBinding = withDefault().currentBinding

  /**
   * Attach described Route to the module.
   */
  def withRoute(route: Route): FintrospectModule = {
    new FintrospectModule(basePath, renderer, route :: theRoutes,
      currentBinding.orElse(route.toPf(basePath)(ValidateParams(route).andThen(Identify(route, basePath)))))
  }

  /**
   * Finaliser for the module builder to combine itself with another module into a Partial Function which matches incoming requests.
   */
  def combine(that: FintrospectModule): Binding = totalBinding.orElse(that.totalBinding)

  /**
   * Finaliser for the module builder to convert itself to a Finagle Service. Use this function when there is only one module.
   */
  def toService: Service[HttpRequest, HttpResponse] = FintrospectModule.toService(totalBinding)
}
