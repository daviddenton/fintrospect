package io.fintrospect.parameters

import com.twitter.finagle.http.Message

/**
  * Parameters which are bound to request/response headers
  */
object Header {

  type Param[F, T, B <: Binding] = Parameter with Extractable[F, T] with Bindable[T, B]

  trait Opt[T] extends io.fintrospect.parameters.Optional[Message, T]
  with ExtractableParameter[Message, T]
  with OptionalRebind[Message, T, RequestBinding]
  with OptionalBindable[T, RequestBinding] {
    self: Param[Message, T, RequestBinding] =>
  }

  trait Mand[T] extends io.fintrospect.parameters.Mandatory[Message, T]
  with ExtractableParameter[Message, T]
  with MandatoryRebind[Message, T, RequestBinding] {
    self: Param[Message, T, RequestBinding] =>
  }

  trait Mandatory[T] extends Mand[T] {
    self: Param[Message, T, RequestBinding] =>
  }

  trait MandatorySeq[T] extends Mand[Seq[T]] {
    self: Param[Message, Seq[T], RequestBinding] =>
  }

  trait Optional[T] extends Opt[T] {
    self: Param[Message, T, RequestBinding] =>
  }

  trait OptionalSeq[T] extends Opt[Seq[T]] {
    self: Param[Message, Seq[T], RequestBinding] =>
  }

  val required = new Parameters[HeaderParameter, Mandatory] with MultiParameters[MultiHeaderParameter, MandatorySeq] {
    override def apply[T](spec: ParameterSpec[T]) = new SingleHeaderParameter(spec) with Mandatory[T]

    override val multi = new Parameters[MultiHeaderParameter, MandatorySeq] {
      override def apply[T](spec: ParameterSpec[T]) = new MultiHeaderParameter(spec) with MandatorySeq[T]
    }
  }

  val optional = new Parameters[HeaderParameter, Optional] with MultiParameters[MultiHeaderParameter, OptionalSeq] {
    override def apply[T](spec: ParameterSpec[T]) = new SingleHeaderParameter(spec) with Optional[T]

    override val multi = new Parameters[MultiHeaderParameter, OptionalSeq] {
      override def apply[T](spec: ParameterSpec[T]) = new MultiHeaderParameter(spec) with OptionalSeq[T]
    }

  }
}
