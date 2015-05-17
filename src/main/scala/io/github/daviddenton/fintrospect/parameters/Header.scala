package io.github.daviddenton.fintrospect.parameters

import io.github.daviddenton.fintrospect.FinagleTypeAliases.FTRequest

/**
 * Builder for parameters that are encoded as HTTP headers.
 */
object Header {

  private val location = new Location {
    override def toString = "header"
    override def from(name: String, request: FTRequest): Option[String] = Option(request.headers().get(name))
  }

  val required = new Parameters(RequiredRequestParameter.builderFor(location))
  val optional = new Parameters(OptionalRequestParameter.builderFor(location))
}
