package examples.json4s

import com.twitter.finagle.Service
import com.twitter.finagle.http.Method._
import com.twitter.finagle.http.Status._
import com.twitter.finagle.http.{Request, Response}
import io.fintrospect.ContentTypes._
import io.fintrospect._
import io.fintrospect.formats.ResponseBuilder._
import io.fintrospect.formats.json.Json4s.Native.JsonFormat
import io.fintrospect.formats.json.Json4s.Native.JsonFormat._
import io.fintrospect.formats.json.Json4s.Native.ResponseBuilder._
import io.fintrospect.parameters._

class AddMessage(emails: Emails) {
  private val exampleEmail = Email(EmailAddress("you@github.com"), EmailAddress("wife@github.com"), "when are you going to be home for dinner", 250)

  private val email = Body(BodySpec[Email](Option("email"), APPLICATION_JSON, s => decode[Email](parse(s)), e => compact(encode(e))), exampleEmail, ObjectParamType)

  private def addEmail() = Service.mk[Request, Response] {
    request => {
      val newEmail = email <-- request
      emails.add(newEmail)
      Ok(encode(emails.forUser(newEmail.to)))
    }
  }


  val route = RouteSpec("add an email and return the new inbox contents for the receiver")
    .body(email)
    .returning(ResponseSpec.json(Ok -> "new list of emails for the 'to' user", encode(Seq(exampleEmail)), JsonFormat))
    .at(Post) / "email" bindTo addEmail
}


