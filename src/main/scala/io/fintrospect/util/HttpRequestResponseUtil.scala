package io.fintrospect.util

import com.twitter.io.Charsets
import org.jboss.netty.handler.codec.http.{HttpMessage, HttpResponse, HttpResponseStatus}

import scala.collection.JavaConversions

object HttpRequestResponseUtil {
  def contentFrom(msg: HttpMessage): String = msg.getContent.toString(Charsets.Utf8)
  def statusAndContentFrom(msg: HttpResponse): (HttpResponseStatus, String) = (msg.getStatus, contentFrom(msg))

  def headersFrom(msg: HttpMessage) : Map[String, String] = {
    val headers = JavaConversions.iterableAsScalaIterable(msg.headers())
    Map(headers.map(entry => entry.getKey -> entry.getValue).toList: _*)
  }
}
