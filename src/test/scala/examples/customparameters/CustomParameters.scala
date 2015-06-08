package examples.customparameters

import com.twitter.finagle.http.Request
import io.fintrospect.parameters.Query

/**
 * This example shows how to define a custom parameter types which can be retrieved from a request. They can be defined and then
 * used alongside the other normal parameters in exactly the same way.
 */
object CustomParameters extends App {

  case class EmailAddress(value: String)

  def emailFrom(value: String): EmailAddress = {
    val emailPattern = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$".r
    emailPattern.findFirstIn(value).map(EmailAddress).get
  }

  val myOptionalEmailParameter = Query.optional.custom("theEmailAddress", emailFrom)

  println("missing email: " + myOptionalEmailParameter.from(Request("/")))
  println("invalid email: " + myOptionalEmailParameter.from(Request("/", "theEmailAddress" -> "notARealEmailAddress")))
  println("valid email: " + myOptionalEmailParameter.from(Request("/", "theEmailAddress" -> "myemail@somedomain.com")))
}
