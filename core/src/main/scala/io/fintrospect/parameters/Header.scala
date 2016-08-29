package io.fintrospect.parameters

import com.twitter.finagle.http.Message

/**
  * Parameters which are bound to request/response headers
  */
object Header {

  type Mandatory[T] = MandatoryParameter[Message, T, RequestBinding]

  type MandatorySeq[T] = MandatoryParameter[Message, Seq[T], RequestBinding]

  type Optional[T] = OptionalParameter[Message, T, RequestBinding]

  type OptionalSeq[T] = OptionalParameter[Message, Seq[T], RequestBinding]

  val required = new Parameters[HeaderParameter, Mandatory] with MultiParameters[MultiHeaderParameter, MandatorySeq] {
    override def apply[T](spec: ParameterSpec[T]) = new SingleParameter(spec, HeaderExtractAndRebind) with HeaderParameter[T] with Mandatory[T]

    override val multi = new Parameters[MultiHeaderParameter, MandatorySeq] {
      override def apply[T](spec: ParameterSpec[T]) = new MultiHeaderParameter(spec) with MandatorySeq[T]
    }
  }

  val optional = new Parameters[HeaderParameter, Optional] with MultiParameters[MultiHeaderParameter, OptionalSeq] {
    override def apply[T](spec: ParameterSpec[T]) = new SingleParameter(spec, HeaderExtractAndRebind) with HeaderParameter[T] with Optional[T]

    override val multi = new Parameters[MultiHeaderParameter, OptionalSeq] {
      override def apply[T](spec: ParameterSpec[T]) = new MultiHeaderParameter(spec) with OptionalSeq[T]
    }

  }
}
