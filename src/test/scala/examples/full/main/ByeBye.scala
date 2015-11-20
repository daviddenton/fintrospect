package examples.full.main

import com.twitter.finagle.Service
import com.twitter.finagle.http.Method.Post
import com.twitter.finagle.http.Status._
import com.twitter.finagle.http.{Request, Response}
import io.fintrospect.formats.text.PlainTextResponseBuilder._
import io.fintrospect.parameters.{ParameterSpec, Query, StringParamType}
import io.fintrospect.{RouteSpec, ServerRoutes}

import scala.language.reflectiveCalls

class ByeBye(entryLogger: EntryLogger) extends ServerRoutes {

  private val username = Query.required(ParameterSpec[Username]("username", None, StringParamType, s => Username(s), _.value.toString))

  private def userExit() = new Service[Request, Response] {
    override def apply(request: Request) = {
      entryLogger.exit(username <-- request).map(ue => Ok())
    }
  }

  add(RouteSpec("User exits the building")
    .taking(SimpleAuthChecker.key)
    .taking(username)
    .returning(Ok -> "Exit granted")
    .returning(NotFound -> "User was not in building")
    .returning(Unauthorized -> "Incorrect key")
    .at(Post) / "bye" bindTo userExit)
}
