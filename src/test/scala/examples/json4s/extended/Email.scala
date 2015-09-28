package examples.json4s.extended

case class Email(to: EmailAddress, from: EmailAddress, subject: String, read: Boolean)

