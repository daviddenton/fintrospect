package examples.json4s

import com.twitter.finagle.Service
import examples.json4s.InboxApp.JsonLibrary.ResponseBuilder.Ok
import io.fintrospect._
import io.fintrospect.formats.ResponseBuilder._
import io.fintrospect.formats.json.Json4s
import io.fintrospect.formats.json.Json4s.Native.JsonFormat
import io.fintrospect.formats.json.Json4s.Native.JsonFormat._
import io.fintrospect.parameters.{ParameterSpec, Path, StringParamType}
import org.jboss.netty.handler.codec.http.HttpMethod._
import org.jboss.netty.handler.codec.http.HttpResponseStatus._
import org.jboss.netty.handler.codec.http.{HttpRequest, HttpResponse}

class EmailList(emails: Emails) {
  private val emailAddress = Path(ParameterSpec[EmailAddress]("address", Option("user email"), StringParamType, EmailAddress, e => e.address))

  private val exampleEmail = Email(EmailAddress("you@github.com"), EmailAddress("wife@github.com"), "when are you going to be home for dinner", 250)

  private def forUser(emailAddress: EmailAddress) = new Service[HttpRequest, HttpResponse] {
    override def apply(request: HttpRequest) = Ok(encode(emails.forUser(emailAddress)))
  }

  val route = RouteSpec("list the inbox contents")
    .returning(ResponseSpec.json(OK -> "list of emails for a user", encode(exampleEmail), JsonFormat))
    .at(GET) / "user" / emailAddress bindTo forUser
}


