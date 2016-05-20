package io.fintrospect.parameters

import com.twitter.finagle.http.Request

/**
  * Parameters which are bound to the query part of a URL
  */
object Query {

  trait Mandatory[T] extends io.fintrospect.parameters.Mandatory[Request, T]
  with ExtractableParameter[Request, T]
  with MandatoryRebind[Request, T, QueryBinding] {
    self: Parameter with Extractable[Request, T] with Bindable[T, QueryBinding] =>
  }

  trait MandatorySeq[T] extends io.fintrospect.parameters.Mandatory[Request, Seq[T]]
  with ExtractableParameter[Request, Seq[T]]
  with MandatoryRebind[Request, Seq[T], QueryBinding] {
    self: Parameter with Extractable[Request, Seq[T]] with Bindable[Seq[T], QueryBinding] =>
  }

  trait Optional[T] extends io.fintrospect.parameters.Optional[Request, T]
  with ExtractableParameter[Request, T]
  with OptionalBindable[T, QueryBinding]
  with OptionalRebind[Request, T, QueryBinding] {
    self: Parameter with Extractable[Request, T] with Bindable[T, QueryBinding] =>
  }

  trait OptionalSeq[T] extends io.fintrospect.parameters.Optional[Request, Seq[T]]
  with ExtractableParameter[Request, Seq[T]]
  with OptionalBindable[Seq[T], QueryBinding]
  with OptionalRebind[Request, Seq[T], QueryBinding] {
    self: Parameter with Extractable[Request, Seq[T]] with Bindable[Seq[T], QueryBinding] =>
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
