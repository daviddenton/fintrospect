package io.github.daviddenton.fintrospect.parameters

import org.jboss.netty.handler.codec.http.HttpRequest

trait Location {
  def from(name: String, request: HttpRequest): Option[String]
}
