package examples.json4s

import com.twitter.finagle.Service
import com.twitter.finagle.http.Method._
import com.twitter.finagle.http.Status.Ok
import com.twitter.finagle.http._
import examples.json4s.InboxApp.JsonLibrary.ResponseBuilder._
import io.fintrospect._
import io.fintrospect.formats.ResponseBuilder._
import io.fintrospect.formats.json.Json4s.Native.JsonFormat
import io.fintrospect.formats.json.Json4s.Native.JsonFormat._
import io.fintrospect.parameters.{ParameterSpec, Path, StringParamType}

class EmailList(emails: Emails) {
  private val emailAddress = Path(ParameterSpec[EmailAddress]("address", Option("user email"), StringParamType, EmailAddress, e => e.address))

  private val exampleEmail = Email(EmailAddress("you@github.com"), EmailAddress("wife@github.com"), "when are you going to be home for dinner", 250)

  private def forUser(emailAddress: EmailAddress) = Service.mk[Request, Response] { _ => Ok(encode(emails.forUser(emailAddress))) }

  val route = RouteSpec("list the inbox contents")
    .returning(ResponseSpec.json(Ok -> "list of emails for a user", encode(Seq(exampleEmail)), JsonFormat))
    .at(Get) / "user" / emailAddress bindTo forUser
}


