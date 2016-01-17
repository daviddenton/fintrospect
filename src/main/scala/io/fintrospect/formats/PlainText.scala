package io.fintrospect.formats

import io.fintrospect.ContentTypes

case class PlainTextValue(value: String)

object PlainText {
  object ResponseBuilder extends ResponseBuilderMethods[PlainTextValue] {
    override def HttpResponse() = new ResponseBuilder[PlainTextValue](_.value, PlainTextValue, e => PlainTextValue(e.getMessage), ContentTypes.TEXT_PLAIN)
  }
}