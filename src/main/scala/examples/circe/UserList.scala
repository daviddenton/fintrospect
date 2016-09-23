package examples.circe

import com.twitter.finagle.Service
import com.twitter.finagle.http.Method.Get
import com.twitter.finagle.http.Request
import com.twitter.finagle.http.Status.Ok
import com.twitter.util.Future
import io.circe.generic.auto._
import io.fintrospect.RouteSpec
import io.fintrospect.formats.Circe
import io.fintrospect.formats.Circe.JsonFormat.responseSpec

/**
  * This endpoint uses the "Circe.Filters.AutoOut" Filter to automatically create a HTTP 200 response from some returned case class content.
  */
class UserList(emails: Emails) {

  private val list: Service[Request, Set[EmailAddress]] = Service.mk { req: Request => Future.value(emails.users()) }

  val route = RouteSpec("list the known users on this server")
    .returning(responseSpec(Ok -> "all users who have sent or received a mail", EmailAddress("you@github.com")))
    .at(Get) / "user" bindTo Circe.Filters.AutoOut[Request, Set[EmailAddress]]().andThen(list)
}


