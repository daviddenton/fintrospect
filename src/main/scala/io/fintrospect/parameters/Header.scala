package io.fintrospect.parameters

import org.jboss.netty.handler.codec.http.HttpRequest

import scala.util.Try

/**
 * Builder for parameters that are encoded as HTTP headers.
 */
object Header {

  private val location = new Location {
    override def toString = "header"

    override def from(name: String, request: HttpRequest): Option[String] = Option(request.headers().get(name))
  }

  val required = new Parameters[MandatoryRequestParameter] {
    override protected def parameter[T](name: String, description: Option[String], paramType: ParamType, parse: (String => Try[T])) =
      new MandatoryRequestParameter[T](name, location, description, paramType, parse)
  }

  val optional = new Parameters[OptionalRequestParameter] {
    override protected def parameter[T](name: String, description: Option[String], paramType: ParamType, parse: (String => Try[T])) =
      new OptionalRequestParameter[T](name, location, description, paramType, parse)
  }
}
