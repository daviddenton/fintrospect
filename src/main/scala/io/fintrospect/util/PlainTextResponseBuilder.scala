package io.fintrospect.util

import io.fintrospect.ContentTypes

case class PlainText(value: String)

object PlainTextResponseBuilder extends ResponseBuilderMethods[PlainText] {
  override def Response() = new ResponseBuilder[PlainText](_.value, PlainText, e => PlainText(e.getMessage), ContentTypes.TEXT_PLAIN)
}