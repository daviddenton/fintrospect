package examples.full.main

import com.twitter.finagle.Service
import com.twitter.finagle.http.Method.Post
import com.twitter.finagle.http.Status._
import com.twitter.finagle.http.{Request, Response}
import io.fintrospect.formats.text.PlainTextResponseBuilder._
import io.fintrospect.parameters.{ParameterSpec, Query, StringParamType}
import io.fintrospect.{RouteSpec, ServerRoutes}

import scala.language.reflectiveCalls

class KnockKnock(userDirectory: UserDirectory, entryLogger: EntryLogger) extends ServerRoutes {
  private val username = Query.required(ParameterSpec[Username]("username", None, StringParamType, s => Username(s), _.value.toString))

  private def userEntry() = new Service[Request, Response] {
    override def apply(request: Request) = {
      userDirectory.lookup(username <-- request)
        .flatMap {
          case Some(user) => entryLogger.enter(user.name).map(ue => Ok())
          case None => NotFound()
        }
    }
  }

  add(RouteSpec("User enters the building")
    .taking(SimpleAuthChecker.key)
    .taking(username)
    .returning(Ok -> "Access granted")
    .returning(NotFound -> "Unknown user")
    .returning(Unauthorized -> "Incorrect key")
    .at(Post) / "knock" bindTo userEntry)
}
