package examples.full.main

import com.twitter.finagle.Service
import com.twitter.finagle.http.Method.Get
import com.twitter.finagle.http.Request
import com.twitter.util.Future
import io.fintrospect.renderers.View
import io.fintrospect.{RouteSpec, ServerRoutes}

class ShowKnownUsers(userDirectory: UserDirectory) extends ServerRoutes[View] {

  private def show() =
    new Service[Request, View]() {
      override def apply(request: Request) = {
        userDirectory.list()
          .flatMap(u => Future.value(KnownUsers(u)))

      }
    }

  add(RouteSpec("See current inhabitants of building").at(Get) / "known" bindTo show)

}
