package io.github.daviddenton.fintrospect

import argo.jdom.JsonRootNode
import com.twitter.finagle.http.filter.Cors.{HttpFilter, UnsafePermissivePolicy}
import com.twitter.finagle.http.path.{Path, _}
import com.twitter.finagle.http.service.RoutingService
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.util.Future
import io.github.daviddenton.fintrospect.FintrospectModule._
import io.github.daviddenton.fintrospect.util.ArgoUtil.pretty
import org.jboss.netty.buffer.ChannelBuffers._
import org.jboss.netty.handler.codec.http.HttpMethod
import org.jboss.netty.util.CharsetUtil._

object FintrospectModule {
  val IDENTIFY_SVC_HEADER = "descriptionServiceId"

  private type Svc = Service[Request, Response]
  private type Binding = PartialFunction[(HttpMethod, Path), Svc]
  private type SM[P] = SegmentMatcher[P]

  def toService(binding: Binding): Svc = RoutingService.byMethodAndPathObject(binding)

  def apply[Ds <: Description](rootPath: Path, defaultRender: Seq[ModuleRoute[Ds]] => JsonRootNode): FintrospectModule[Ds] = new FintrospectModule[Ds](rootPath, defaultRender, Nil, PartialFunction.empty[(HttpMethod, Path), Svc])
}

class FintrospectModule[Ds <: Description] private(private val rootPath: Path, private val defaultRender: Seq[ModuleRoute[Ds]] => JsonRootNode, private val moduleRoutes: List[ModuleRoute[Ds]], private val userRoutes: Binding) {

  private case class Identify(moduleRoute: ModuleRoute[Ds]) extends SimpleFilter[Request, Response]() {
    override def apply(request: Request, service: Service[Request, Response]): Future[Response] = {
      request.headers().set(IDENTIFY_SVC_HEADER, request.getMethod() + ":" + moduleRoute.toString())
      service(request)
    }
  }

  private case class RoutesToJson(jsonStr: String) extends Service[Request, Response]() {
    override def apply(request: Request): Future[Response] = {
      request.headers().set(IDENTIFY_SVC_HEADER, request.getMethod() + s":${request.getUri()}")
      val response = Response()
      response.setStatusCode(200)
      response.setContent(copiedBuffer(jsonStr, UTF_8))
      Future.value(response)
    }
  }

  private def withDefault() = {
    new FintrospectModule[Ds](rootPath, defaultRender, moduleRoutes, userRoutes.orElse({
      case HttpMethod.GET -> p if p == rootPath => new HttpFilter(UnsafePermissivePolicy).andThen(RoutesToJson(pretty(defaultRender(moduleRoutes))))
    }))
  }

  private def withDescribedRoute(description: Ds, sm: SM[_]*)(bindFn: Identify => Binding): FintrospectModule[Ds] = {
    val moduleRoute = new ModuleRoute(description, rootPath, sm)
    new FintrospectModule[Ds](rootPath, defaultRender, moduleRoute :: moduleRoutes, userRoutes.orElse(bindFn(Identify(moduleRoute))))
  }

  def withRoute(description: Ds, fn: () => Svc) = withDescribedRoute(description) {
    identify => {
      case method -> path if description.matches(method, rootPath, path) => identify.andThen(fn())
    }
  }

  def withRoute[A](description: Ds, sm0: SM[A], fn: A => Svc) = withDescribedRoute(description, sm0) {
    identify => {
      case method -> path / sm0(s0) if description.matches(method, rootPath, path) => identify.andThen(fn(s0))
    }
  }

  def withRoute[A, B](description: Ds, sm0: SM[A], sm1: SM[B], fn: (A, B) => Svc) = withDescribedRoute(description, sm0, sm1) {
    identify => {
      case method -> path / sm0(s0) / sm1(s1) if description.matches(method, rootPath, path) => identify.andThen(fn(s0, s1))
    }
  }

  def withRoute[A, B, C](description: Ds, sm0: SM[A], sm1: SM[B], sm2: SM[C], fn: (A, B, C) => Svc) = withDescribedRoute(description, sm0, sm1, sm2) {
    identify => {
      case method -> path / sm0(s0) / sm1(s1) / sm2(s2) if description.matches(method, rootPath, path) => identify.andThen(fn(s0, s1, s2))
    }
  }

  def withRoute[A, B, C, D](description: Ds, sm0: SM[A], sm1: SM[B], sm2: SM[C], sm3: SM[D], fn: (A, B, C, D) => Svc) = withDescribedRoute(description, sm0, sm1, sm2, sm3) {
    identify => {
      case method -> path / sm0(s0) / sm1(s1) / sm2(s2) / sm3(s3) if description.matches(method, rootPath, path) => identify.andThen(fn(s0, s1, s2, s3))
    }
  }

  def withRoute[A, B, C, D, E](description: Ds, sm0: SM[A], sm1: SM[B], sm2: SM[C], sm3: SM[D], sm4: SM[E], fn: (A, B, C, D, E) => Svc) = withDescribedRoute(description, sm0, sm1, sm2, sm3, sm4) {
    identify => {
      case method -> path / sm0(s0) / sm1(s1) / sm2(s2) / sm3(s3) / sm4(s4) if description.matches(method, rootPath, path) => identify.andThen(fn(s0, s1, s2, s3, s4))
    }
  }

  def withRoute[A, B, C, D, E, F](description: Ds, sm0: SM[A], sm1: SM[B], sm2: SM[C], sm3: SM[D], sm4: SM[E], sm5: SM[F], fn: (A, B, C, D, E, F) => Svc) = withDescribedRoute(description, sm0, sm1, sm2, sm3, sm4, sm5) {
    identify => {
      case method -> path / sm0(s0) / sm1(s1) / sm2(s2) / sm3(s3) / sm4(s4) / sm5(s5) if description.matches(method, rootPath, path) => identify.andThen(fn(s0, s1, s2, s3, s4, s5))
    }
  }

  def routes = withDefault().userRoutes

  def toService = FintrospectModule.toService(routes)
}
