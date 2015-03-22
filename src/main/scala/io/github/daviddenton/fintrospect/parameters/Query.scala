package io.github.daviddenton.fintrospect.parameters

import com.twitter.finagle.http.Request

object Query {
  private val location = new Location {
    override def toString = "query"

    override def from(name: String, request: Request): Option[String] = {
      request.params.get(name)
    }
  }

  val required = new Parameters(RequiredRequestParameter.builderForLocation(location))
  val optional = new Parameters(OptionalRequestParameter.builderForLocation(location))
}