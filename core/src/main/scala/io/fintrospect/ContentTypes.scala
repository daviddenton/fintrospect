package io.fintrospect

import javax.activation.MimetypesFileTypeMap

import com.twitter.finagle.http.Message
import io.fintrospect.parameters.Header

case class ContentType(value: String) extends AnyVal

object ContentType {
  private lazy val extMap = new MimetypesFileTypeMap(getClass.getResourceAsStream("/META-INF/mime.types"))

  def lookup(name: String): ContentType = ContentType(extMap.getContentType(name))

  val header = Header.optional.string("Content-Type")
  private val accept = Header.optional.*.string("Accept")

  def fromAcceptHeaders(msg: Message): Option[Set[ContentType]] =
    accept.from(msg)
      .map(s => {
        s.flatMap(
          value => value.split(Array(',', ' ', ';'))
            .map(_.toLowerCase)
            .filter(_.matches(".+\\/.+"))
            .map(ContentType(_))
        ).toSet
      })
}

object ContentTypes {

  val APPLICATION_ATOM_XML = ContentType("application/atom+xml")
  val APPLICATION_FORM_URLENCODED = ContentType("application/x-www-form-urlencoded")
  val APPLICATION_JSON = ContentType("application/json")
  val APPLICATION_OCTET_STREAM = ContentType("application/octet-stream")
  val APPLICATION_SVG_XML = ContentType("application/svg+xml")
  val APPLICATION_XHTML_XML = ContentType("application/xhtml+xml")
  val APPLICATION_XML = ContentType("application/xml")
  val APPLICATION_X_MSGPACK = ContentType("application/x-msgpack")
  val TEXT_HTML = ContentType("text/html")
  val TEXT_PLAIN = ContentType("text/plain")
  val TEXT_XML = ContentType("text/xml")
  val WILDCARD = ContentType("*/*")
}
