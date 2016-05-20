package io.fintrospect.parameters

import com.twitter.finagle.http.Message
import io.fintrospect.Extractable

/**
  * Parameters which are bound to request/response headers
  */
object Header {

  trait Mandatory[T] extends io.fintrospect.parameters.Mandatory[Message, T]
  with ExtractableParameter[Message, T]
  with MandatoryRebind[Message, T, RequestBinding] {
    self: Parameter with Extractable[Message, T] with Bindable[T, RequestBinding] =>
  }

  trait MandatorySeq[T] extends io.fintrospect.parameters.Mandatory[Message, Seq[T]]
  with ExtractableParameter[Message, Seq[T]]
  with MandatoryRebind[Message, Seq[T], RequestBinding] {
    self: Parameter with Extractable[Message, Seq[T]] with Bindable[Seq[T], RequestBinding] =>
  }

  trait Optional[T] extends io.fintrospect.parameters.Optional[Message, T]
  with ExtractableParameter[Message, T]
  with OptionalRebind[Message, T, RequestBinding]
  with OptionalBindable[T, RequestBinding] {
    self: Parameter with Extractable[Message, T] with Bindable[T, RequestBinding] =>
  }

  trait OptionalSeq[T] extends io.fintrospect.parameters.Optional[Message, Seq[T]]
  with ExtractableParameter[Message, Seq[T]]
  with OptionalRebind[Message, Seq[T], RequestBinding]
  with OptionalBindable[Seq[T], RequestBinding] {
    self: Parameter with Extractable[Message, Seq[T]] with Bindable[Seq[T], RequestBinding] =>
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
