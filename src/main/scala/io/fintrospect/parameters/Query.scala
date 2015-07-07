package io.fintrospect.parameters

import org.jboss.netty.handler.codec.http.HttpRequest

object Query {

  trait Mandatory[T] extends io.fintrospect.parameters.Mandatory[T, HttpRequest] with MandatoryRebind[T, HttpRequest, QueryBinding] {
    self: Bindable[T, QueryBinding] =>
  }

  trait Optional[T] extends io.fintrospect.parameters.Optional[T, HttpRequest] with OptionalRebind[T, HttpRequest, QueryBinding] {
    self: Bindable[T, QueryBinding] =>
  }

  val required = new Parameters[QueryParameter, Mandatory] {
    override def apply[T](spec: ParameterSpec[T]) = new QueryParameter[T](spec) with Mandatory[T]
  }

  val optional = new Parameters[QueryParameter, Optional] {
    override def apply[T](spec: ParameterSpec[T]) = new QueryParameter[T](spec) with Optional[T]
  }
}