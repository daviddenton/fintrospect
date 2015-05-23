package io.github.daviddenton.fintrospect

import argo.jdom.JsonRootNode
import com.twitter.finagle.http.path.Path
import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.util.Future
import io.github.daviddenton.fintrospect.FintrospectModule._
import io.github.daviddenton.fintrospect.Routing.fromBinding
import io.github.daviddenton.fintrospect.parameters.Requirement._
import io.github.daviddenton.fintrospect.util.ResponseBuilder._
import io.github.daviddenton.fintrospect.util.{ResponseBuilder, TypedResponseBuilder}
import org.jboss.netty.handler.codec.http.HttpMethod.GET
import org.jboss.netty.handler.codec.http.{HttpMethod, HttpRequest, HttpResponse}

import scala.PartialFunction._

object FintrospectModule {

  private type Binding = PartialFunction[(HttpMethod, Path), Service[HttpRequest, HttpResponse]]

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
   * Create a module using the given base-path and description descRenderer.
   */
  def apply(basePath: Path, descRenderer: Renderer, responseRenderer: TypedResponseBuilder[JsonRootNode] = ResponseBuilder.Json): FintrospectModule = {
    new FintrospectModule(basePath, descRenderer, responseRenderer, Nil, empty[(HttpMethod, Path), Service[HttpRequest, HttpResponse]])
  }

  private case class ValidateParams(route: Route, responseRenderer: TypedResponseBuilder[JsonRootNode]) extends SimpleFilter[HttpRequest, HttpResponse]() {
    override def apply(request: HttpRequest, service: Service[HttpRequest, HttpResponse]): Future[HttpResponse] = {
      val paramsAndParseResults = route.describedRoute.params.map(p => (p, p.parseFrom(request)))
      val withoutMissingOptionalParams = paramsAndParseResults.filterNot(pr => pr._1.requirement == Optional && pr._2.isEmpty)
      val missingOrFailed = withoutMissingOptionalParams.filterNot(pr => pr._2.isDefined && pr._2.get.isSuccess).map(_._1)
      if (missingOrFailed.isEmpty) service(request) else responseRenderer.BadRequest(missingOrFailed)
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
class FintrospectModule private(basePath: Path, descRenderer: Renderer, responseRenderer: TypedResponseBuilder[JsonRootNode], theRoutes: List[Route], private val currentBinding: Binding) {
  private def withDefault() = withRoute(DescribedRoute("Description route").at(GET).bindTo(() => {
    Service.mk((req) => Renderer.toResponse(descRenderer(basePath, theRoutes)))
  }))

  private def totalBinding = withDefault().currentBinding

  /**
   * Attach described Route to the module.
   */
  def withRoute(route: Route): FintrospectModule = {
    new FintrospectModule(basePath, descRenderer, responseRenderer, route :: theRoutes,
      currentBinding.orElse(route.toPf(basePath)(ValidateParams(route, responseRenderer).andThen(Identify(route, basePath)))))
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
