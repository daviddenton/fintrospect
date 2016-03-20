package examples.json4s

import com.twitter.finagle.Service
import com.twitter.finagle.http.Method.Post
import com.twitter.finagle.http.Status.Ok
import com.twitter.finagle.http.{Request, Response}
import io.fintrospect.RouteSpec
import io.fintrospect.formats.json.Json4s.Native.JsonFormat.{bodySpec, encode, responseSpec}
import io.fintrospect.formats.json.Json4s.Native.ResponseBuilder.statusToResponseBuilderConfig
import io.fintrospect.parameters.{Body, ObjectParamType}




class AddMessage(emails: Emails) {
  private val exampleEmail = Email(EmailAddress("you@github.com"), EmailAddress("wife@github.com"), "when are you going to be home for dinner", 250)

  private val email = Body(bodySpec[Email](Option("email")), exampleEmail, ObjectParamType)

  private def addEmail() = Service.mk[Request, Response] {
    request => {
      val newEmail = email <-- request
      emails.add(newEmail)
      Ok(encode(emails.forUser(newEmail.to)))
    }
  }


  val route = RouteSpec("add an email and return the new inbox contents for the receiver")
    .body(email)
    .returning(responseSpec(Ok -> "new list of emails for the 'to' user", Seq(exampleEmail)))
    .at(Post) / "email" bindTo addEmail
}


