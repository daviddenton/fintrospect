package examples.full.main

import java.time.Clock

import com.twitter.finagle.Http
import com.twitter.finagle.http.Method.{Get, Post}
import com.twitter.finagle.http.{Response, Status}
import com.twitter.util.Future
import examples.full.main.EntryLogger.{Log, LogList}
import io.fintrospect.RouteSpec
import io.fintrospect.formats.json.Json4s.Native.JsonFormat._
import io.fintrospect.parameters.Body

object EntryLogger {

  object Log {
    val userEntry = Body(bodySpec[UserEntry]())
    val route = RouteSpec().body(userEntry).at(Post) / "log"
  }

  object LogList {
    val entries = Body(bodySpec[Seq[UserEntry]]())
    val route = RouteSpec().at(Get) / "list"
  }
}

/**
  * Remote User Entry Logger service, accessible over HTTP
  */
class EntryLogger(hostAuthority: String, clock: Clock) {
  private val http = Http.newService(hostAuthority)

  private val client = Log.route bindToClient http

  private def expect[T](expectedStatus: Status, b: Body[T]): Response => T = {
    r => if (r.status == expectedStatus) b <-- r else throw RemoteSystemProblem("entry logger", r.status)
  }

  def log(user: User): Future[UserEntry] = client(Log.userEntry --> UserEntry(user.name.value, clock.instant().toEpochMilli))
    .map(expect(Status.Created, Log.userEntry))

  private val listClient = LogList.route bindToClient http

  def list(): Future[Seq[UserEntry]] = listClient().map(expect(Status.Ok, LogList.entries))
}
