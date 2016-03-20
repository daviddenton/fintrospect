package examples.json4s

import com.twitter.finagle.Service
import com.twitter.finagle.http.Method.Get
import com.twitter.finagle.http.Status.Ok
import com.twitter.finagle.http.{Request, Response}
import examples.json4s.InboxApp.JsonLibrary.JsonFormat.{encode, responseSpec}
import examples.json4s.InboxApp.JsonLibrary.ResponseBuilder.implicits.statusToResponseBuilderConfig
import io.fintrospect.RouteSpec

class UserList(emails: Emails) {
  private def list() = Service.mk[Request, Response] { _ => Ok(encode(emails.users())) }

  val route = RouteSpec("list the known users on this server")
    .returning(responseSpec(Ok -> "all users who have sent or received a mail", EmailAddress("you@github.com")))
    .at(Get) / "user" bindTo list
}


