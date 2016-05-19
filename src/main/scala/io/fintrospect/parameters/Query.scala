package io.fintrospect.parameters

import com.twitter.finagle.http.Request

/**
  * Parameters which are bound to the query part of a URL
  */
object Query {

  trait Mandatory[T] extends io.fintrospect.parameters.Mandatory[T, Request]
  with ExtractableParameter[T, Request]
  with MandatoryRebind[T, Request, QueryBinding] {
    self: Parameter with Extractable[T, Request] with Bindable[T, QueryBinding] =>
  }

  trait MandatorySeq[T] extends io.fintrospect.parameters.Mandatory[Seq[T], Request]
  with ExtractableParameter[Seq[T], Request]
  with MandatoryRebind[Seq[T], Request, QueryBinding] {
    self: Parameter with Extractable[Seq[T], Request] with Bindable[Seq[T], QueryBinding] =>
  }

  trait Optional[T] extends io.fintrospect.parameters.Optional[T, Request]
  with ExtractableParameter[T, Request]
  with OptionalBindable[T, QueryBinding]
  with OptionalRebind[T, Request, QueryBinding] {
    self: Parameter with Extractable[T, Request] with Bindable[T, QueryBinding] =>
  }

  trait OptionalSeq[T] extends io.fintrospect.parameters.Optional[Seq[T], Request]
  with ExtractableParameter[Seq[T], Request]
  with OptionalBindable[Seq[T], QueryBinding]
  with OptionalRebind[Seq[T], Request, QueryBinding] {
    self: Parameter with Extractable[Seq[T], Request] with Bindable[Seq[T], QueryBinding] =>
  }

  val required = new Parameters[QueryParameter, Mandatory] with MultiParameters[MultiQueryParameter, MandatorySeq] {
    override def apply[T](spec: ParameterSpec[T]) = new SingleQueryParameter(spec) with Mandatory[T]

    override val multi = new Parameters[MultiQueryParameter, MandatorySeq] {
      override def apply[T](spec: ParameterSpec[T]) = new MultiQueryParameter(spec) with MandatorySeq[T]
    }
  }

  val optional = new Parameters[QueryParameter, Optional] with MultiParameters[MultiQueryParameter, OptionalSeq] {
    override def apply[T](spec: ParameterSpec[T]) = new SingleQueryParameter(spec) with Optional[T]

    override val multi = new Parameters[MultiQueryParameter, OptionalSeq] {
      override def apply[T](spec: ParameterSpec[T]) = new MultiQueryParameter(spec) with OptionalSeq[T]
    }
  }
}
