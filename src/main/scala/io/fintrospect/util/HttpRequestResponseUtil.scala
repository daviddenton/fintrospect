package io.fintrospect.util

import com.twitter.io.Charsets
import org.jboss.netty.handler.codec.http.{HttpMessage, HttpResponse, HttpResponseStatus}

object HttpRequestResponseUtil {
  def contentFrom(msg: HttpMessage): String = msg.getContent.toString(Charsets.Utf8)
  def statusAndContentFrom(msg: HttpResponse): (HttpResponseStatus, String) = (msg.getStatus, contentFrom(msg))
}
