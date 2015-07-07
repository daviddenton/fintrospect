package io.fintrospect.parameters

import org.jboss.netty.handler.codec.http.HttpRequest


object Header {

  trait Mandatory[T] extends io.fintrospect.parameters.Mandatory[T, HttpRequest] with MandatoryRebind[T, HttpRequest, RequestBinding] {
    self: Bindable[T, RequestBinding] =>
  }

  trait Optional[T] extends io.fintrospect.parameters.Optional[T, HttpRequest] with OptionalRebind[T, HttpRequest, RequestBinding] {
    self: Bindable[T, RequestBinding] =>
  }


  val required = new Parameters[HeaderParameter, Mandatory] {
    override def apply[T](spec: ParameterSpec[T]) = new HeaderParameter[T](spec)
      with Mandatory[T]
  }

  val optional = new Parameters[HeaderParameter, Optional] {
    override def apply[T](spec: ParameterSpec[T]) = new HeaderParameter[T](spec)
      with Optional[T]
  }
}
