package io.fintrospect.parameters

import org.jboss.netty.handler.codec.http.HttpRequest



object Header {

  private val location = new Location {
    override def toString = "header"

    override def from(name: String, request: HttpRequest) = Option(request.headers().get(name))

    override def into(name: String, value: String, request: HttpRequest) = request.headers().add(name, value)
  }

  trait Mandatory[T] extends io.fintrospect.parameters.Mandatory[T, HttpRequest]

  trait Optional[T] extends io.fintrospect.parameters.Optional[T, HttpRequest]

  val required = new Parameters[HeaderParameter, Mandatory] {
    override def apply[T](spec: ParameterSpec[T]) = new HeaderParameter[T](spec, location) with Mandatory[T]
  }

  val optional = new Parameters[HeaderParameter, Optional] {
    override def apply[T](spec: ParameterSpec[T]) = new HeaderParameter[T](spec, location) with Optional[T]
  }
}
