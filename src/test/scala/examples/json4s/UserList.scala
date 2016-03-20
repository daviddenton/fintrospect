package examples.json4s

import com.twitter.finagle.Service
import com.twitter.finagle.http.Method.Get
import com.twitter.finagle.http.Status._
import com.twitter.finagle.http.{Request, Response}
import examples.json4s.InboxApp.JsonLibrary.JsonFormat.encode
import examples.json4s.InboxApp.JsonLibrary.JsonFormat.responseSpec
import examples.json4s.InboxApp.JsonLibrary.ResponseBuilder._
import io.fintrospect.RouteSpec

class UserList(emails: Emails) {
  private def list() = Service.mk[Request, Response] { _ => Ok(encode(emails.users())) }

  val route = RouteSpec("list the known users on this server")
    .returning(responseSpec(Ok -> "all users who have sent or received a mail", EmailAddress("you@github.com")))
    .at(Get) / "user" bindTo list
}


