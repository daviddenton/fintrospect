package io.github.daviddenton.fintrospect.testing

import com.twitter.finagle.http.path.Root
import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.Await
import io.github.daviddenton.fintrospect.renderers.SimpleJson
import io.github.daviddenton.fintrospect.{FintrospectModule, Route}

trait TestingFintrospectRoute {
  val route: Route

  def responseFor(request: Request): Response = {
    Await.result(FintrospectModule(Root, SimpleJson())
      .withRoute(route).toService.apply(request))
  }
}
