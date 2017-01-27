package examples.circe


import com.twitter.finagle.Service
import com.twitter.finagle.http.Method.Get
import com.twitter.finagle.http.{Request, Response, Status}
import io.circe.generic.auto._
import io.fintrospect.RouteSpec
import io.fintrospect.formats.Circe.JsonFormat.encode
import io.fintrospect.formats.Circe.ResponseBuilder._
import io.fintrospect.formats.Circe.responseSpec
import io.fintrospect.parameters.Path

/**
  * This endpoint shows how to manually create an HTTP response with some Circe-encoded content.
  */
class EmailList(emails: Emails) {
  private val exampleEmail = Email(EmailAddress("you@github.com"), EmailAddress("wife@github.com"), "when are you going to be home for dinner", 250)

  private def forUser(emailAddress: EmailAddress): Service[Request, Response] =
    Service.mk { _ => Ok(encode(emails.forUser(emailAddress))) }

  val route = RouteSpec("list the inbox contents")
    .returning(responseSpec(Status.Ok -> "list of emails for a user", Seq(exampleEmail)))
    .at(Get) / "emails" / Path(EmailAddress.spec, "address", "user email") bindTo forUser
}


