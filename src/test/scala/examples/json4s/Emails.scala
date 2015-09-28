package examples.json4s

trait Emails {
  def users(): Set[EmailAddress]

  def forUser(searchAddress: EmailAddress): Seq[Email]
}

object Emails {
  class InMemoryEmails extends Emails {
    private val allEmails = Seq(
      Email(EmailAddress("me@fintrospect.io"), EmailAddress("you@fintrospect.io"), "second email", false),
      Email(EmailAddress("me@fintrospect.io"), EmailAddress("you@fintrospect.io"), "first email", true)
    )

    override def forUser(searchAddress: EmailAddress): Seq[Email] = allEmails.filter(_.to.equals(searchAddress))

    override def users(): Set[EmailAddress] = allEmails.flatMap(e => Seq(e.from, e.to)).toSet
  }
}
