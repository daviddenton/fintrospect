package examples.circe

import com.twitter.finagle.Service
import com.twitter.finagle.http.Method.Get
import com.twitter.finagle.http.Request
import com.twitter.finagle.http.Status.{NotFound, Ok}
import com.twitter.util.Future
import io.circe.generic.auto._
import io.fintrospect.RouteSpec
import io.fintrospect.formats.Circe.Auto._
import io.fintrospect.parameters.Path

/**
  * This endpoint uses the "Circe.Auto.OptionalOut" Filter to automatically create a HTTP 200 response from a Some
  * returned case class content. If the service returns None, a 404 is generated.
  */
class FindUserWithEmail(emails: Emails) {

  private def findByEmail(email: EmailAddress) = {
    val lookupUserByEmail: Service[Request, Option[EmailAddress]] =
      Service.mk { _: Request => Future(emails.users().find(_.address == email.address)) }

    OptionalOut(lookupUserByEmail)
  }

  val route = RouteSpec("Get the user for the particular email address")
    .returning(Ok -> "found the user")
    .returning(NotFound -> "who is that?")
    .at(Get) / "user" / Path(EmailAddress.spec) bindTo findByEmail
}


