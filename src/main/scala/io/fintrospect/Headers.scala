package io.fintrospect

import com.twitter.finagle.http.Message
import io.fintrospect.parameters.Retrieval

case class HttpHeader(name: String) extends Retrieval[Option[String], Message] {
  def of(value: String): MessageBinding = new MessageBinding {
    def apply(msg: Message) = msg.headerMap(name) = value
  }

  def <--(msg: Message) = msg.headerMap.get(name)
}

object Headers {
  val IdentifyRouteName = HttpHeader("X-Fintrospect-Route-Name")
  val CacheControl = HttpHeader("Cache-Control")
  val Date = HttpHeader("Date")
  val ETag = HttpHeader("ETag")
  val Authorization = HttpHeader("Authorization")
  val Expires = HttpHeader("Expires")
  val Host = HttpHeader("Host")
  val Vary = HttpHeader("Vary")

  @deprecated("use IdentifyRouteName instead", "12.11.0")
  val IDENTIFY_SVC_HEADER = IdentifyRouteName.name
}
