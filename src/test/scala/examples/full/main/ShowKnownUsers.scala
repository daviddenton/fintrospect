package examples.full.main

import com.twitter.finagle.Service
import com.twitter.finagle.http.Method.Get
import com.twitter.finagle.http.Request
import com.twitter.util.Future
import io.fintrospect.formats.Html
import io.fintrospect.renderers.{RenderMustacheView, View}
import io.fintrospect.{RouteSpec, ServerRoutes}

class ShowKnownUsers(userDirectory: UserDirectory) extends ServerRoutes {

  private def show() =
    new RenderMustacheView(Html.ResponseBuilder)
      .andThen(new Service[Request, View]() {
        override def apply(request: Request) = {
          userDirectory.list()
          .flatMap(u => Future.value(KnownUsers(u)))

        }
      })

  add(RouteSpec("See current inhabitants of building").at(Get) / "known" bindTo show)

}
