package examples.full.main

import com.twitter.finagle.Service
import com.twitter.finagle.http.Method.Post
import com.twitter.finagle.http.Status._
import com.twitter.finagle.http.{Request, Response}
import examples.full.main.SecuritySystemAuth.apiKey
import io.fintrospect.formats.PlainText.ResponseBuilder._
import io.fintrospect.parameters.{ParameterSpec, Query, StringParamType}
import io.fintrospect.{RouteSpec, ServerRoutes}

import scala.language.reflectiveCalls

class ByeBye(inhabitants: Inhabitants, entryLogger: EntryLogger) extends ServerRoutes {

  private val username = Query.required(ParameterSpec[Username]("username", None, StringParamType, s => Username(s), _.value.toString))

  private def userExit() = new Service[Request, Response] {
    override def apply(request: Request) = {
      val exiting = username <-- request
      if (inhabitants.remove(exiting))
        entryLogger
          .exit(exiting)
          .map(ue => Accepted())
      else BadRequest()
    }
  }

  add(RouteSpec("User exits the building")
    .taking(apiKey) // see SecuritySystemAuth for why this is here
    .taking(username)
    .returning(Ok -> "Exit granted")
    .returning(NotFound -> "User was not in building")
    .returning(BadRequest -> "User is not inside building")
    .returning(Unauthorized -> "Incorrect key")
    .at(Post) / "bye" bindTo userExit)
}
