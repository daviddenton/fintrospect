package io.fintrospect.formats

import com.twitter.io.Buf
import io.fintrospect.ContentTypes

/**
  * Native (string-based) HTML support (text/html content type)
  */
object Html {
  case class $ private[Html](value: String)

  object ResponseBuilder extends AbstractResponseBuilder[$] {
    def HttpResponse(): ResponseBuilder[$] = new ResponseBuilder[$](i => Buf.Utf8(i.value), $, e => $(e.getMessage), ContentTypes.TEXT_HTML)
  }
}