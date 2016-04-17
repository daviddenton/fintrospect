package examples.circe

import com.twitter.finagle.Service
import com.twitter.finagle.http.Method.Get
import com.twitter.finagle.http.Status.Ok
import com.twitter.finagle.http.{Request, Response}
import io.fintrospect.formats.json.Circe.JsonFormat.{encode, responseSpec}
import io.fintrospect.formats.json.Circe.ResponseBuilder.implicits.statusToResponseBuilderConfig
import io.fintrospect.RouteSpec
import io.circe.generic.auto._

class UserList(emails: Emails) {
  private val list = Service.mk[Request, Response] { _ => Ok(encode(emails.users())) }

  val route = RouteSpec("list the known users on this server")
    .returning(responseSpec(Ok -> "all users who have sent or received a mail", EmailAddress("you@github.com")))
    .at(Get) / "user" bindTo list
}


