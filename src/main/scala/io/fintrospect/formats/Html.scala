package io.fintrospect.formats

import io.fintrospect.ContentTypes

/**
  * Native (string-based) HTML support (text/html content type)
  */
object Html {
  case class $ private[Html](value: String)

  object ResponseBuilder extends ResponseBuilderMethods[$] {
    def HttpResponse(): ResponseBuilder[$] = new ResponseBuilder[$](_.value, $, e => $(e.getMessage), ContentTypes.TEXT_HTML)
  }
}