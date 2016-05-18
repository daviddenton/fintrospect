package io.fintrospect.parameters

import com.twitter.finagle.http.Message

/**
  * Parameters which are bound to request/response headers
  */
object Header {

  trait Mandatory[T] extends io.fintrospect.parameters.Mandatory[T, Message]
  with ValidatableParameter[T, Message]
  with MandatoryRebind[T, Message, RequestBinding] {
    self: Parameter with Validatable[T, Message] with Bindable[T, RequestBinding] =>
  }

  trait MandatorySeq[T] extends io.fintrospect.parameters.Mandatory[Seq[T], Message]
  with ValidatableParameter[Seq[T], Message]
  with MandatoryRebind[Seq[T], Message, RequestBinding] {
    self: Parameter with Validatable[Seq[T], Message] with Bindable[Seq[T], RequestBinding] =>
  }

  trait Optional[T] extends io.fintrospect.parameters.Optional[T, Message]
  with ValidatableParameter[T, Message]
  with OptionalRebind[T, Message, RequestBinding]
  with OptionalBindable[T, RequestBinding] {
    self: Parameter with Validatable[T, Message] with Bindable[T, RequestBinding] =>
  }

  trait OptionalSeq[T] extends io.fintrospect.parameters.Optional[Seq[T], Message]
  with ValidatableParameter[Seq[T], Message]
  with OptionalRebind[Seq[T], Message, RequestBinding]
  with OptionalBindable[Seq[T], RequestBinding] {
    self: Parameter with Validatable[Seq[T], Message] with Bindable[Seq[T], RequestBinding] =>
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
