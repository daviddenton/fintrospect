package examples.json4s.extended

import com.twitter.finagle.Service
import com.twitter.util.Future
import examples.json4s.extended.InboxApp.JsonLibrary.JsonFormat
import examples.json4s.extended.InboxApp.JsonLibrary.ResponseBuilder.Ok
import io.fintrospect._
import io.fintrospect.parameters.Path
import org.jboss.netty.handler.codec.http.HttpMethod._
import org.jboss.netty.handler.codec.http.HttpResponseStatus._
import org.jboss.netty.handler.codec.http.{HttpRequest, HttpResponse}
import org.json4s.JsonAST.{JArray, JValue}

class EmailList(emails: Emails) {
  private val exampleEmail = Email(EmailAddress("you@github.com"), EmailAddress("wife@github.com"), "when are you going to be home for dinner", false)

  private def forUser(emailAddress: EmailAddress) = new Service[HttpRequest, HttpResponse] {
    override def apply(request: HttpRequest): Future[HttpResponse] = Future.value(Ok(asJson(emails.forUser(emailAddress): _*)) )
  }

  private def asJson(emails: Email*): JValue = JArray(emails.map(Email.encodeJson).toList)

  val route = RouteSpec("list the inbox contents")
    .returning(ResponseSpec.json(OK -> "list of emails for a user", asJson(exampleEmail), JsonFormat))
    .at(GET) / "user" / Path(Email.Spec) bindTo forUser
}


