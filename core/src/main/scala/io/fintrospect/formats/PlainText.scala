package io.fintrospect.formats

import com.twitter.io.Buf
import io.fintrospect.ContentTypes


/**
  * Native (string-based) Text support (text/plain content type)
  */
object PlainText {

  case class $ private[PlainText](value: String)

  object ResponseBuilder extends AbstractResponseBuilder[$] {
    def HttpResponse(): ResponseBuilder[$] = new ResponseBuilder[$](i => Buf.Utf8(i.value), $, e => $(e.getMessage), ContentTypes.TEXT_PLAIN)
  }
}