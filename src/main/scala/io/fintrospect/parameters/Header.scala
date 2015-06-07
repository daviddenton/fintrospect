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

  val required = new Parameters[RequestParameter, Mandatory] {
    override protected def parameter[T](name: String, description: Option[String], paramType: ParamType, parse: (String => Try[T])) =
      new RequestParameter[T](name, description, paramType, location, parse) with Mandatory[T]
  }

  val optional = new Parameters[RequestParameter, Optional] {
    override protected def parameter[T](name: String, description: Option[String], paramType: ParamType, parse: (String => Try[T])) =
      new RequestParameter[T](name, description, paramType, location, parse) with Optional[T]
  }
}
