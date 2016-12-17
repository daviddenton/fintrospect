package examples.customparameters

import com.twitter.finagle.http.Request
import io.fintrospect.parameters._

import scala.language.reflectiveCalls

/**
  * This example shows how to define a custom parameter types which can be retrieved from a request. They can be defined and then
  * used alongside the other normal parameters in exactly the same way.
  */

// Custom domain type
case class EmailAddress(value: String)

// EmailAddress how the parameters are serialized from/to the target Parameter type
object EmailAddress {
  private def emailFrom(value: String): EmailAddress = {
    val emailPattern = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$".r
    emailPattern.findFirstIn(value).map(EmailAddress(_)).get
  }

  private def emailTo(email: EmailAddress): String = email.value

  val spec = ParameterSpec.string("theEmailAddress", "a valid email address").map(emailFrom, emailTo)
}

object CustomParameters extends App {

  // Reuse the spec in different parts of the request
  val optionalEmailQueryParameter = Query.optional(EmailAddress.spec)
  val requiredEmailHeader = Header.required(EmailAddress.spec)
  val requiredEmailPathSegment = Path(EmailAddress.spec)

  println("missing email: " + (optionalEmailQueryParameter <-- Request("/")))
  println("valid email: " + (optionalEmailQueryParameter <-- Request("/", "theEmailAddress" -> "myemail@somedomain.com")))
}
