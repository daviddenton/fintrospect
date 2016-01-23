package examples.full.test.env

import com.twitter.finagle.Service
import com.twitter.finagle.http.Status._
import com.twitter.finagle.http.{Request, Response}
import examples.full.main.EntryLogger._
import examples.full.main._
import io.fintrospect.ServerRoutes
import io.fintrospect.formats.json.Json4s.Native.JsonFormat._
import io.fintrospect.formats.json.Json4s.Native.ResponseBuilder._

/**
  * Fake implementation of the Entry Logger HTTP contract. Note the re-use of the RouteSpecs from EntryLogger.
  */
class FakeEntryLoggerState extends ServerRoutes[Response] {

  var entries: Seq[UserEntry] = null

  def reset() = entries = Seq[UserEntry]()

  private def entry() = new Service[Request, Response] {
    override def apply(request: Request) = {
      val userEntry = Entry.entry <-- request
      entries = entries :+ userEntry
      Created(encode(userEntry))
    }
  }

  add(Entry.route.bindTo(entry))

  private def exit() = new Service[Request, Response] {
    override def apply(request: Request) = {
      val userEntry = Exit.entry <-- request
      entries = entries :+ userEntry
      Created(encode(userEntry))
    }
  }

  add(Exit.route.bindTo(entry))

  private def entryList() = new Service[Request, Response] {
    override def apply(request: Request) = Ok(encode(entries))
  }

  add(LogList.route.bindTo(entryList))

  reset()
}
