package io.fintrospect.parameters

import org.jboss.netty.handler.codec.http.HttpRequest

/**
 * Builder for parameters that are encoded as HTTP headers.
 */
object Header {

  private val location = new Location {
    override def toString = "header"
    override def from(name: String, request: HttpRequest): Option[String] = Option(request.headers().get(name))
  }

  val required = new Parameters(RequiredRequestParameter.builder(location))
  val optional = new Parameters(OptionalRequestParameter.builder(location))
}
