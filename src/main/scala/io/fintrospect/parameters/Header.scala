package io.fintrospect.parameters

import com.twitter.finagle.http.Message

/**
  * Parameters which are bound to request/response headers
  */
object Header {

  trait Mandatory[T] extends io.fintrospect.parameters.Mandatory[T, Message]
  with MandatoryRebind[T, Message, RequestBinding] {
    self: Bindable[T, RequestBinding] =>
  }

  trait Optional[T] extends io.fintrospect.parameters.Optional[T, Message]
  with OptionalBindable[T, RequestBinding]
  with OptionalRebind[T, Message, RequestBinding] {
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
