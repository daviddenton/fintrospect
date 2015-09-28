package examples.json4s

case class Email(to: EmailAddress, from: EmailAddress, subject: String, read: Boolean)

