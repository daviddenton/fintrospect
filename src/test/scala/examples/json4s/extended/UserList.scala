package examples.json4s.extended

import com.twitter.finagle.Service
import com.twitter.util.Future
import examples.json4s.extended.InboxApp.JsonLibrary.JsonFormat
import examples.json4s.extended.InboxApp.JsonLibrary.ResponseBuilder.Ok
import io.fintrospect._
import org.jboss.netty.handler.codec.http.HttpMethod._
import org.jboss.netty.handler.codec.http.HttpResponseStatus._
import org.jboss.netty.handler.codec.http.{HttpRequest, HttpResponse}
import org.json4s.JsonAST.{JArray, JString, JValue}

class UserList(emails: Emails) {
  private def list() = new Service[HttpRequest, HttpResponse] {
    override def apply(request: HttpRequest): Future[HttpResponse] = Future.value(Ok(asJson(emails.users().toSeq: _*)))
  }

  private def asJson(emails: EmailAddress*): JValue = JArray(emails.map(a => JString(a.address)).toList)

  val route = RouteSpec("list the known users on this server")
    .returning(ResponseSpec.json(OK -> "all users who have sent or received a mail", asJson(EmailAddress("you@github.com")), JsonFormat))
    .at(GET) / "user" bindTo list
}


