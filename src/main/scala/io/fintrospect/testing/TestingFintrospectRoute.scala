package io.fintrospect.testing

import java.util.concurrent.TimeUnit

import com.twitter.finagle.http.path.Root
import com.twitter.util.Await
import io.fintrospect.renderers.simplejson.SimpleJson
import io.fintrospect.{FintrospectModule, Route}
import org.jboss.netty.handler.codec.http.{HttpRequest, HttpResponse}

import scala.concurrent.duration.Duration

/**
 * Mixin this and provide the route under test.
 */
trait TestingFintrospectRoute {
  val route: Route

  /**
   * get the response from the Route under test for this response. Default timeout is massively generous
   * @param request to apply
   * @param timeout defaults to 1s.
   * @return response
   */
  def responseFor(request: HttpRequest, timeout: Duration = Duration(1, TimeUnit.SECONDS)): HttpResponse = {
    Await.result(FintrospectModule(Root, SimpleJson()).withRoute(route).toService.apply(request))
  }
}
