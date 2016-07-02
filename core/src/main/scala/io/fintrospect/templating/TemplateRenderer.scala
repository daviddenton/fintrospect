package io.fintrospect.templating

import com.twitter.io.Buf

trait TemplateRenderer {
  def toBuf(view: View): Buf
}
