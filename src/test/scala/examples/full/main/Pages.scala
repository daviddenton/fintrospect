package examples.full.main

import java.time.LocalDateTime

import com.twitter.finagle.Service
import com.twitter.finagle.http.Method.Get
import com.twitter.finagle.http.Request
import io.fintrospect.templating.View
import io.fintrospect.{RouteSpec, ServerRoutes}

case class Index(time: String, browser: String) extends View

class Pages(userDirectory: UserDirectory) extends ServerRoutes[View] {

  private def show() = Service.mk[Request, View] {
    request => userDirectory.list().flatMap(u => KnownUsers(u))
  }

  private def index() = Service.mk[Request, View] {
    request => Index(LocalDateTime.now().toString, request.headerMap("User-Agent"))
  }

  add(RouteSpec("See current inhabitants of building").at(Get) / "known" bindTo show)
  add(RouteSpec("Index").at(Get) bindTo index)

}
