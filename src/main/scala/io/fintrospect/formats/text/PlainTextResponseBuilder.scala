package io.fintrospect.formats.text

import io.fintrospect.ContentTypes
import io.fintrospect.formats.{ResponseBuilder, ResponseBuilderMethods}

object PlainTextResponseBuilder extends ResponseBuilderMethods[PlainText] {
  override def Response() = new ResponseBuilder[PlainText](_.value, PlainText, e => PlainText(e.getMessage), ContentTypes.TEXT_PLAIN)
}