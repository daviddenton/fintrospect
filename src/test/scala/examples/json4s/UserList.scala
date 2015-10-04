package examples.json4s

import com.twitter.finagle.Service
import com.twitter.finagle.httpx.Method._
import com.twitter.finagle.httpx.{Request, Response, Status}
import examples.json4s.InboxApp.JsonLibrary.JsonFormat
import examples.json4s.InboxApp.JsonLibrary.JsonFormat._
import examples.json4s.InboxApp.JsonLibrary.ResponseBuilder.OK
import io.fintrospect._
import io.fintrospect.formats.ResponseBuilder._

class UserList(emails: Emails) {
  private def list() = new Service[Request, Response] {
    override def apply(request: Request) = OK(encode(emails.users()))
  }

  val route = RouteSpec("list the known users on this server")
    .returning(ResponseSpec.json(Status.Ok -> "all users who have sent or received a mail", encode(EmailAddress("you@github.com")), JsonFormat))
    .at(Get) / "user" bindTo list
}


