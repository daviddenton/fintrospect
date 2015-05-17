package io.github.daviddenton.fintrospect.parameters

import io.github.daviddenton.fintrospect.FinagleTypeAliases.FTRequest
import io.github.daviddenton.fintrospect.RequestParams

/**
 * Builder for parameters that are encoded in the HTTP form.
 */
object Form {
  private val location = new Location {
    override def toString = "query"

    override def from(name: String, request: FTRequest): Option[String] = {
      new RequestParams(request).get(name)
    }
  }

  val required = new Parameters(RequiredRequestParameter.builderFor(location))
  val optional = new Parameters(OptionalRequestParameter.builderFor(location))
}