package examples.circe

import com.twitter.finagle.Service
import com.twitter.finagle.http.Method.Post
import com.twitter.finagle.http.Status.Ok
import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.Future
import io.circe.generic.auto._
import io.fintrospect.RouteSpec
import io.fintrospect.formats.Circe
import io.fintrospect.formats.Circe.JsonFormat.{bodySpec, responseSpec}
import io.fintrospect.parameters.{Body, ObjectParamType, Path}

/**
  * This endpoint uses the "Circe.Filters.AutoInOut" Filter to automatically create a HTTP 200 response from some returned case class content.
  */
class AddMessage(emails: Emails) {
  private val exampleEmail = Email(EmailAddress("you@github.com"), EmailAddress("wife@github.com"), "when are you going to be home for dinner", 250)

  private val email = Body(bodySpec[Email](Option("email")), exampleEmail, ObjectParamType)

  private def addEmail(address: EmailAddress): Service[Request, Response] =
    Circe.Filters.AutoInOut(
      Service.mk {
        newEmail: Email => {
          // validate that the receiver is as passed as the one in the URL
          if (address == newEmail.to) emails.add(newEmail)
          Future.value(emails.forUser(newEmail.to))
        }
      }
    )

  val route = RouteSpec("add an email and return the new inbox contents for the receiver")
    .body(email)
    .returning(responseSpec(Ok -> "new list of emails for the 'to' user", Seq(exampleEmail)))
    .at(Post) / "email" / Path(EmailAddress.spec) bindTo addEmail
}


