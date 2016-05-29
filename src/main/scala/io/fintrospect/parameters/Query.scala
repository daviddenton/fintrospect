package io.fintrospect.parameters

import com.twitter.finagle.http.Request

/**
  * Parameters which are bound to the query part of a URL
  */
object Query {

  type Param[F, T, B <: Binding] = Parameter with Extractable[F, T] with Bindable[T, B]

  trait Opt[T] extends io.fintrospect.parameters.Optional[Request, T]
  with ExtractableParameter[Request, T]
  with OptionalRebind[Request, T, QueryBinding]
  with OptionalBindable[T, QueryBinding] {
    self: Param[Request, T, QueryBinding] =>
  }

  trait Mand[T] extends io.fintrospect.parameters.Mandatory[Request, T]
  with ExtractableParameter[Request, T]
  with MandatoryRebind[Request, T, QueryBinding] {
    self: Param[Request, T, QueryBinding] =>
  }

  trait Mandatory[T] extends Mand[T] {
    self: Param[Request, T, QueryBinding] =>
  }

  trait MandatorySeq[T] extends Mand[Seq[T]] {
    self: Param[Request, Seq[T], QueryBinding] =>
  }

  trait Optional[T] extends Opt[T] {
    self: Param[Request, T, QueryBinding] =>
  }

  trait OptionalSeq[T] extends Opt[Seq[T]] {
    self: Param[Request, Seq[T], QueryBinding] =>
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
