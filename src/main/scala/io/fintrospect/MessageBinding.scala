package io.fintrospect

import com.twitter.finagle.http.Message

trait MessageBinding {
  def apply(msg: Message)
}
