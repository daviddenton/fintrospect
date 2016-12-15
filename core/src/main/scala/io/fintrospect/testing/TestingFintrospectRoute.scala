package io.fintrospect.testing

import java.util.concurrent.TimeUnit

import com.twitter.finagle.http.path.Root
import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.Await
import io.fintrospect.renderers.simplejson.SimpleJson
import io.fintrospect.{RouteModule, ServerRoute}

import scala.concurrent.duration.Duration

/**
  * Mixin this and provide the route under test.
  */
trait TestingFintrospectRoute {
  val route: ServerRoute[Request, Response]

  /**
    * get the response from the Route under test for this response. Default timeout is massively generous
    *
    * @param request to apply
    * @param timeout defaults to 1s.
    * @return response
    */
  def responseFor(request: Request, timeout: Duration = Duration(1, TimeUnit.SECONDS)): Response =
    Await.result(RouteModule(Root, SimpleJson()).withRoute(route).toService.apply(request))
}
