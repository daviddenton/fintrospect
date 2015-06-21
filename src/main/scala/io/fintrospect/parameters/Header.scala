package io.fintrospect.parameters

import org.jboss.netty.handler.codec.http.HttpRequest



object Header {

  private val location = new Location {
    override def toString = "header"

    override def from(name: String, request: HttpRequest): Option[String] = Option(request.headers().get(name))

    override def into(name: String, value: String, request: HttpRequest): Unit = request.headers().add(name, value)
  }

  trait Mandatory[T] extends io.fintrospect.parameters.Mandatory[T, HttpRequest]

  trait Optional[T] extends io.fintrospect.parameters.Optional[T, HttpRequest]

  val required = new Parameters[RequestParameter, Mandatory] {
    override def apply[T](spec: ParameterSpec[T]): RequestParameter[T] with Mandatory[T] = new RequestParameter[T](spec, location) with Mandatory[T]
  }

  val optional = new Parameters[RequestParameter, Optional] {
    override def apply[T](spec: ParameterSpec[T]): RequestParameter[T] with Optional[T] = new RequestParameter[T](spec, location) with Optional[T]
  }
}
