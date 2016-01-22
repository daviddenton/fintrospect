package io.fintrospect.formats

import io.fintrospect.ContentTypes


/**
  * Native (string-based) Text support (text/plain content type)
  */
object PlainText {

  case class $ private[PlainText](value: String)

  object ResponseBuilder extends AbstractResponseBuilder[$] {
    def HttpResponse(): ResponseBuilder[$] = new ResponseBuilder[$](_.value, $, e => $(e.getMessage), ContentTypes.TEXT_PLAIN)
  }
}