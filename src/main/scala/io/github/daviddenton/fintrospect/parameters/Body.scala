package io.github.daviddenton.fintrospect.parameters

import com.twitter.finagle.http.Request

object Body {
  private val location = new Location {
    override def toString = "body"

    override def from(name: String, request: Request): Option[String] = {
      Some(request.contentString)
    }
  }

  val required = new Parameters(RequiredRequestParameter.builderFor(location))
  val optional = new Parameters(OptionalRequestParameter.builderFor(location))
}