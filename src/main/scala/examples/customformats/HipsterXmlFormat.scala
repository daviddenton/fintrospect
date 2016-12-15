package examples.customformats

/**
  * @param value the content to encase in html
  */
case class HipsterXmlFormat(value: String) {
  override def toString: String = value

  def asXmlMessage: String = s"<envelope><body>$value</body></envelope>"
}

object HipsterXmlFormat {
  def apply(children: HipsterXmlFormat*): HipsterXmlFormat = HipsterXmlFormat(children.map(_.toString()).mkString(""))
}
