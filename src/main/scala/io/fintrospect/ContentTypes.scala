package io.fintrospect

case class ContentType(value: String)

object ContentTypes {

  val APPLICATION_ATOM_XML = ContentType("application/atom+xml")
  val APPLICATION_FORM_URLENCODED = ContentType("application/x-www-form-urlencoded")
  val APPLICATION_JSON = ContentType("application/json")
  val APPLICATION_OCTET_STREAM = ContentType("application/octet-stream")
  val APPLICATION_SVG_XML = ContentType("application/svg+xml")
  val APPLICATION_XHTML_XML = ContentType("application/xhtml+xml")
  val APPLICATION_XML = ContentType("application/xml")
  val TEXT_HTML = ContentType("text/html")
  val TEXT_PLAIN = ContentType("text/plain")
  val TEXT_XML = ContentType("text/xml")
  val WILDCARD = ContentType("*/*")
}
