package io.github.daviddenton.fintrospect

case class MimeType(value: String)

object MimeTypes {

  val APPLICATION_ATOM_XML = MimeType("application/atom+xml")
  val APPLICATION_FORM_URLENCODED = MimeType("application/x-www-form-urlencoded")
  val APPLICATION_JSON = MimeType("application/json")
  val APPLICATION_OCTET_STREAM = MimeType("application/octet-stream")
  val APPLICATION_SVG_XML = MimeType("application/svg+xml")
  val APPLICATION_XHTML_XML = MimeType("application/xhtml+xml")
  val APPLICATION_XML = MimeType("application/xml")
  val TEXT_HTML = MimeType("text/html")
  val TEXT_PLAIN = MimeType("text/plain")
  val TEXT_XML = MimeType("text/xml")
  val WILDCARD = MimeType("*/*")
}
