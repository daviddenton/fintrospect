package io.github.daviddenton.fintrospect

import argo.jdom.JsonRootNode
import com.twitter.finagle.http.path.{Path, _}
import com.twitter.finagle.http.service.RoutingService
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.util.Future
import io.github.daviddenton.fintrospect.FintrospectModule._
import io.github.daviddenton.fintrospect.parameters.PathParameter
import io.github.daviddenton.fintrospect.util.ArgoUtil.pretty
import org.jboss.netty.buffer.ChannelBuffers._
import org.jboss.netty.handler.codec.http.HttpMethod
import org.jboss.netty.handler.codec.http.HttpMethod.GET
import org.jboss.netty.util.CharsetUtil._

object FintrospectModule {
  val IDENTIFY_SVC_HEADER = "descriptionServiceId"

  type Renderer = (Seq[ModuleRoute] => JsonRootNode)
  private type Svc = Service[Request, Response]
  private type Binding = PartialFunction[(HttpMethod, Path), Svc]
  private type PP[T] = PathParameter[T]

  def toService(binding: Binding): Svc = RoutingService.byMethodAndPathObject(binding)

  def apply(rootPath: Path, defaultRender: Renderer): FintrospectModule = new FintrospectModule(rootPath, defaultRender, Nil, PartialFunction.empty[(HttpMethod, Path), Svc])
}

class FintrospectModule private(private val rootPath: Path, private val defaultRender: Renderer, private val moduleRoutes: List[ModuleRoute], private val userRoutes: Binding) {

  private case class Identify(moduleRoute: ModuleRoute) extends SimpleFilter[Request, Response]() {
    override def apply(request: Request, service: Service[Request, Response]): Future[Response] = {
      request.headers().set(IDENTIFY_SVC_HEADER, request.getMethod() + ":" + moduleRoute.toString())
      service(request)
    }
  }

  private case class RoutesContent(descriptionContent: String) extends Service[Request, Response]() {
    override def apply(request: Request): Future[Response] = {
      request.headers().set(IDENTIFY_SVC_HEADER, request.getMethod() + s":${request.getUri()}")
      val response = Response()
      response.setStatusCode(200)
      response.setContent(copiedBuffer(descriptionContent, UTF_8))
      Future.value(response)
    }
  }

  private def withDefault() = {
    withRoute(Description("Description route", GET, identity), () => RoutesContent(pretty(defaultRender(moduleRoutes))))
  }

  private def withDescribedRoute(description: Description, PP: PP[_]*)(bindFn: Identify => Binding): FintrospectModule = {
    val moduleRoute = new ModuleRoute(description, rootPath, PP)
    new FintrospectModule(rootPath, defaultRender, moduleRoute :: moduleRoutes, userRoutes.orElse(bindFn(Identify(moduleRoute))))
  }

  def withRoute(description: Description, fn: () => Svc) = withDescribedRoute(description) {
    identify => {
      case method -> path if description.matches(method, rootPath, path) => identify.andThen(fn())
    }
  }

  def withRoute[A](description: Description, PP0: PP[A], fn: A => Svc) = withDescribedRoute(description, PP0) {
    identify => {
      case method -> path / PP0(s0) if description.matches(method, rootPath, path) => identify.andThen(fn(s0))
    }
  }

  def withRoute[A, B](description: Description, PP0: PP[A], PP1: PP[B], fn: (A, B) => Svc) = withDescribedRoute(description, PP0, PP1) {
    identify => {
      case method -> path / PP0(s0) / PP1(s1) if description.matches(method, rootPath, path) => identify.andThen(fn(s0, s1))
    }
  }

  def withRoute[A, B, C](description: Description, PP0: PP[A], PP1: PP[B], PP2: PP[C], fn: (A, B, C) => Svc) = withDescribedRoute(description, PP0, PP1, PP2) {
    identify => {
      case method -> path / PP0(s0) / PP1(s1) / PP2(s2) if description.matches(method, rootPath, path) => identify.andThen(fn(s0, s1, s2))
    }
  }

  def withRoute[A, B, C, D](description: Description, PP0: PP[A], PP1: PP[B], PP2: PP[C], PP3: PP[D], fn: (A, B, C, D) => Svc) = withDescribedRoute(description, PP0, PP1, PP2, PP3) {
    identify => {
      case method -> path / PP0(s0) / PP1(s1) / PP2(s2) / PP3(s3) if description.matches(method, rootPath, path) => identify.andThen(fn(s0, s1, s2, s3))
    }
  }

  def withRoute[A, B, C, D, E](description: Description, PP0: PP[A], PP1: PP[B], PP2: PP[C], PP3: PP[D], PP4: PP[E], fn: (A, B, C, D, E) => Svc) = withDescribedRoute(description, PP0, PP1, PP2, PP3, PP4) {
    identify => {
      case method -> path / PP0(s0) / PP1(s1) / PP2(s2) / PP3(s3) / PP4(s4) if description.matches(method, rootPath, path) => identify.andThen(fn(s0, s1, s2, s3, s4))
    }
  }

  def withRoute[A, B, C, D, E, F](description: Description, PP0: PP[A], PP1: PP[B], PP2: PP[C], PP3: PP[D], PP4: PP[E], PP5: PP[F], fn: (A, B, C, D, E, F) => Svc) = withDescribedRoute(description, PP0, PP1, PP2, PP3, PP4, PP5) {
    identify => {
      case method -> path / PP0(s0) / PP1(s1) / PP2(s2) / PP3(s3) / PP4(s4) / PP5(s5) if description.matches(method, rootPath, path) => identify.andThen(fn(s0, s1, s2, s3, s4, s5))
    }
  }

  def routes = withDefault().userRoutes

  def toService = FintrospectModule.toService(routes)
}
