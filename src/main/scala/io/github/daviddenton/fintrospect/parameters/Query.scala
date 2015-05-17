package io.github.daviddenton.fintrospect.parameters

import io.github.daviddenton.fintrospect.FinagleTypeAliases.FTRequest
import io.github.daviddenton.fintrospect.RequestParamMap

/**
 * Builder for parameters that are encoded in the HTTP query.
 */
object Query {
  private val location = new Location {
    override def toString = "query"

    override def from(name: String, request: FTRequest): Option[String] = {
      new RequestParamMap(request).get(name)
    }
  }

  val required = new Parameters(RequiredRequestParameter.builderFor(location))
  val optional = new Parameters(OptionalRequestParameter.builderFor(location))
}