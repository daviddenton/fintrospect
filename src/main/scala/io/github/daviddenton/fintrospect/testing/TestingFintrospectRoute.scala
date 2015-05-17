package io.github.daviddenton.fintrospect.testing

import java.util.concurrent.TimeUnit

import com.twitter.finagle.http.path.Root
import com.twitter.util.Await
import io.github.daviddenton.fintrospect.FinagleTypeAliases.{FTRequest, FTResponse}
import io.github.daviddenton.fintrospect.renderers.SimpleJson
import io.github.daviddenton.fintrospect.{FintrospectModule, Route}

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
  def responseFor(request: FTRequest, timeout: Duration = Duration(1, TimeUnit.SECONDS)): FTResponse = {
    Await.result(FintrospectModule(Root, SimpleJson()).withRoute(route).toService.apply(request))
  }
}
