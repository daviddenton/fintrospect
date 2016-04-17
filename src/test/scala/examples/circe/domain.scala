package examples.circe

case class Email(to: EmailAddress, from: EmailAddress, subject: String, bytes: Int)

case class EmailAddress(address: String)

