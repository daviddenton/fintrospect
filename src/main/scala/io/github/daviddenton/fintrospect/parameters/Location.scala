package io.github.daviddenton.fintrospect.parameters

import com.twitter.finagle.http.Request

sealed trait Location {
  def from(name: String, request: Request): Option[String]
}

object Locations {

  object PathLocation extends Location {
    override def toString = "path"

    override def from(name: String, request: Request): Option[String] = None
  }

  object HeaderLocation extends Location {
    override def toString = "header"

    override def from(name: String, request: Request): Option[String] = Option(request.headers().get(name))
  }

  object QueryLocation extends Location {
    override def toString = "query"

    override def from(name: String, request: Request): Option[String] = request.params.get(name)
  }

}
