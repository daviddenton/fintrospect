package examples.circe


import com.twitter.finagle.Service
import com.twitter.finagle.http.Method.Get
import com.twitter.finagle.http.Status.Ok
import com.twitter.finagle.http.{Request, Response}
import io.circe.generic.auto._
import io.fintrospect.RouteSpec
import io.fintrospect.formats.Circe.JsonFormat.{encode, responseSpec}
import io.fintrospect.formats.Circe.ResponseBuilder.implicits.statusToResponseBuilderConfig
import io.fintrospect.parameters.Path

/**
  * This endpoint shows how to manually create an HTTP response with some Circe-encoded content.
  */
class EmailList(emails: Emails) {
  private val exampleEmail = Email(EmailAddress("you@github.com"), EmailAddress("wife@github.com"), "when are you going to be home for dinner", 250)

  private def forUser(emailAddress: EmailAddress): Service[Request, Response] =
    Service.mk[Request, Response] { _ => Ok(encode(emails.forUser(emailAddress))) }

  val route = RouteSpec("list the inbox contents")
    .returning(responseSpec(Ok -> "list of emails for a user", Seq(exampleEmail)))
    .at(Get) / "emails" / Path(EmailAddress.spec) bindTo forUser
}


