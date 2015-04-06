package io.github.daviddenton.fintrospect.parameters

import com.twitter.finagle.http.Request

/**
 * Builder for parameters that are encoded as HTTP headers.
 */
object Header {

  private val location = new Location {
    override def toString = "header"
    override def from(name: String, request: Request): Option[String] = Option(request.headers().get(name))
  }

  val required = new Parameters(RequiredRequestParameter.builderFor(location))
  val optional = new Parameters(OptionalRequestParameter.builderFor(location))
}
