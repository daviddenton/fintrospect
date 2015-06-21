package io.fintrospect.parameters

import org.jboss.netty.handler.codec.http.HttpRequest

object Header {

  private val location = new Location {
    override def toString = "header"

    override def from(name: String, request: HttpRequest): Option[String] = Option(request.headers().get(name))

    override def into(name: String, value: String, request: HttpRequest): Unit = request.headers().add(name, value)
  }

  def required[T](spec: ParameterSpec[T]) = new RequestParameter[T](spec, location) with Mandatory[T, HttpRequest]

  def optional[T](spec: ParameterSpec[T]) = new RequestParameter[T](spec, location) with Optional[T, HttpRequest]
}
