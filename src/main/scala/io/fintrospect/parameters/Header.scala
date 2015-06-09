package io.fintrospect.parameters

import org.jboss.netty.handler.codec.http.HttpRequest

/**
 * Builder for parameters that are encoded as HTTP headers.
 */
object Header {

  private val location = new Location {
    override def toString = "header"

    override def from(name: String, request: HttpRequest): Option[String] = Option(request.headers().get(name))

    override def into(name: String, value: String, request: HttpRequest): Unit = {
      request.headers().add(name, value)
    }
  }

  val required = new Parameters[MandatoryRequestParameter, Mandatory] {
    override protected def parameter[T](name: String, description: Option[String], paramType: ParamType,
                                        deserialize: String => T, serialize: T => String) =
      new MandatoryRequestParameter[T](name, location, description, paramType, deserialize, serialize)
  }

  val optional = new Parameters[OptionalRequestParameter, Optional] {
    override protected def parameter[T](name: String, description: Option[String], paramType: ParamType,
                                        deserialize: String => T, serialize: T => String) =
      new OptionalRequestParameter[T](name, location, description, paramType, deserialize, serialize)
  }
}
