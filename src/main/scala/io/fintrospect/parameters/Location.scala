package io.fintrospect.parameters

import org.jboss.netty.handler.codec.http.HttpRequest

trait Location {

  def into(name: String, value: String, request: HttpRequest): Unit

  def from(name: String, request: HttpRequest): Option[String]
}
