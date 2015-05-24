package examples.customformats

/**
 * @param value the content to encase in html
 */
case class XmlFormat(value: String) {
  override def toString(): String = value

  def asXmlMessage: String = s"<envelope><body>$value</body></envelope>"
}

object XmlFormat {
  def apply(children: XmlFormat*): XmlFormat = XmlFormat(children.map(_.toString()).mkString(""))
}
