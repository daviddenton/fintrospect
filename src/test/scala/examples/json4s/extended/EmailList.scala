package examples.json4s.extended

import com.twitter.finagle.Service
import examples.json4s.extended.InboxApp.JsonLibrary.JsonFormat
import examples.json4s.extended.InboxApp.JsonLibrary.JsonFormat._
import examples.json4s.extended.InboxApp.JsonLibrary.ResponseBuilder._
import io.fintrospect._
import io.fintrospect.formats.ResponseBuilder._
import io.fintrospect.parameters.Path
import org.jboss.netty.handler.codec.http.HttpMethod._
import org.jboss.netty.handler.codec.http.HttpResponseStatus._
import org.jboss.netty.handler.codec.http.{HttpRequest, HttpResponse}

class EmailList(emails: Emails) {
  private val exampleEmail = Email(EmailAddress("you@github.com"), EmailAddress("wife@github.com"), "when are you going to be home for dinner", false)

  private def forUser(emailAddress: EmailAddress) = new Service[HttpRequest, HttpResponse] {
    override def apply(request: HttpRequest) = Ok(encode(emails.forUser(emailAddress)))
  }

  val route = RouteSpec("list the inbox contents")
    .returning(ResponseSpec.json(OK -> "list of emails for a user", encode(exampleEmail), JsonFormat))
    .at(GET) / "user" / Path(Email.Spec) bindTo forUser
}


