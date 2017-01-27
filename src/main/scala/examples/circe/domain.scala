package examples.circe

import io.fintrospect.parameters.ParameterSpec

case class Email(to: EmailAddress, from: EmailAddress, subject: String, bytes: Int)

case class EmailAddress(address: String)

object EmailAddress {
  val spec = ParameterSpec.string().map(i => EmailAddress(i), (e: EmailAddress) => e.address)
}
