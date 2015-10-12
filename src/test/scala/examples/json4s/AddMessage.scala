package examples.json4s

import com.twitter.finagle.Service
import com.twitter.finagle.httpx.Method._
import com.twitter.finagle.httpx._
import examples.json4s.InboxApp.JsonLibrary.ResponseBuilder.OK
import io.fintrospect._
import io.fintrospect.formats.ResponseBuilder._
import io.fintrospect.formats.json.Json4s.Native.JsonFormat
import io.fintrospect.formats.json.Json4s.Native.JsonFormat._
import io.fintrospect.parameters._

class AddMessage(emails: Emails) {
  private val emailAddress = Path(ParameterSpec[EmailAddress]("address", Option("user email"), StringParamType, EmailAddress, e => e.address))

  private val exampleEmail = Email(EmailAddress("you@github.com"), EmailAddress("wife@github.com"), "when are you going to be home for dinner", 250)

  private val spec = BodySpec[Email](Option("email"), ContentTypes.APPLICATION_JSON, s => decode[Email](parse(s)), e => compact(encode(e)))
  private val email = Body(spec, exampleEmail, ObjectParamType)

  private def forUser(emailAddress: EmailAddress) = new Service[Request, Response] {
    override def apply(request: Request) = {
      emails.add(email <-- request)
      OK(encode(emails.forUser(emailAddress)))
    }
  }

  val route = RouteSpec("add an email")
    .body(email)
    .returning(ResponseSpec.json(Status.Ok -> "list of emails for a user", encode(exampleEmail), JsonFormat))
    .at(Post) / "user" / emailAddress bindTo forUser
}


