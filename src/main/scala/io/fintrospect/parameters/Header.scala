package io.fintrospect.parameters

import com.twitter.finagle.http.Request

/**
 * Parameters which are bound to request headers
 */
object Header {

  trait Mandatory[T] extends io.fintrospect.parameters.Mandatory[T, Request] with MandatoryRebind[T, Request, RequestBinding] {
    self: Bindable[T, RequestBinding] =>
  }

  trait Optional[T] extends io.fintrospect.parameters.Optional[T, Request] with OptionalRebind[T, Request, RequestBinding] {
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
