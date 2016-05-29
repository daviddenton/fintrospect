package io.fintrospect.parameters

import com.twitter.finagle.http.Message
import io.fintrospect.parameters.types._

/**
  * Parameters which are bound to request/response headers
  */
object Header {

  trait Mandatory[T] extends Mand[Message, T, RequestBinding] {
    self: Param[Message, T, RequestBinding] =>
  }

  trait MandatorySeq[T] extends Mand[Message, Seq[T], RequestBinding] {
    self: Param[Message, Seq[T], RequestBinding] =>
  }

  trait Optional[T] extends Opt[Message, T, RequestBinding] {
    self: Param[Message, T, RequestBinding] =>
  }

  trait OptionalSeq[T] extends Opt[Message, Seq[T], RequestBinding] {
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
