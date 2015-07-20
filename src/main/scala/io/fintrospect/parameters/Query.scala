package io.fintrospect.parameters

import org.jboss.netty.handler.codec.http.HttpRequest

object Query {

  trait Mandatory[T] extends io.fintrospect.parameters.Mandatory[T, HttpRequest] with MandatoryRebind[T, HttpRequest, QueryBinding] {
    self: Bindable[T, QueryBinding] =>
  }

  trait MandatorySeq[T] extends io.fintrospect.parameters.Mandatory[Seq[T], HttpRequest] with MandatoryRebind[Seq[T], HttpRequest, QueryBinding] {
    self: Bindable[Seq[T], QueryBinding] =>
  }

  trait Optional[T] extends io.fintrospect.parameters.Optional[T, HttpRequest] with OptionalRebind[T, HttpRequest, QueryBinding] {
    self: Bindable[T, QueryBinding] =>
  }

  trait OptionalSeq[T] extends io.fintrospect.parameters.Optional[Seq[T], HttpRequest] with OptionalRebind[Seq[T], HttpRequest, QueryBinding] {
    self: Bindable[Seq[T], QueryBinding] =>
  }

  val required = new Parameters[QueryParameter, Mandatory] with MultiParameters[MultiQueryParameter, MandatorySeq] {
    override def apply[T](spec: ParameterSpec[T]) = new SingleQueryParameter[T](spec) with Mandatory[T]

    override val multi = new Parameters[MultiQueryParameter, MandatorySeq] {
      override def apply[T](spec: ParameterSpec[T]) = new MultiQueryParameter[T](spec) with MandatorySeq[T]
    }
  }

  val optional = new Parameters[QueryParameter, Optional] with MultiParameters[MultiQueryParameter, OptionalSeq] {
    override def apply[T](spec: ParameterSpec[T]) = new SingleQueryParameter[T](spec) with Optional[T]

    override val multi = new Parameters[MultiQueryParameter, OptionalSeq] {
      override def apply[T](spec: ParameterSpec[T]) = new MultiQueryParameter[T](spec) with OptionalSeq[T]
    }
  }
}
