package examples.circe

import io.fintrospect.parameters.{ParameterSpec, StringParamType}

case class Email(to: EmailAddress, from: EmailAddress, subject: String, bytes: Int)

case class EmailAddress(address: String)

object EmailAddress {
  val spec = ParameterSpec[EmailAddress]("address", Option("user email"), StringParamType, EmailAddress(_), e => e.address)
}
