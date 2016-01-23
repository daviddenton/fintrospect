package examples.json4s

import com.twitter.finagle.Service
import com.twitter.finagle.http.Method.Get
import com.twitter.finagle.http.{Response, Request}
import com.twitter.finagle.http.Status._
import examples.json4s.InboxApp.JsonLibrary.JsonFormat
import examples.json4s.InboxApp.JsonLibrary.JsonFormat._
import examples.json4s.InboxApp.JsonLibrary.ResponseBuilder._
import io.fintrospect.{ResponseSpec, RouteSpec}

class UserList(emails: Emails) {
  private def list() = Service.mk[Request, Response] { _ => Ok(encode(emails.users())) }

  val route = RouteSpec("list the known users on this server")
    .returning(ResponseSpec.json(Ok -> "all users who have sent or received a mail", encode(EmailAddress("you@github.com")), JsonFormat))
    .at(Get) / "user" bindTo list
}


