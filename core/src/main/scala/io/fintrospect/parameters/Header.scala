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

  type HSeq[T] = HeaderParameter[Seq[T]]

  val required = new Parameters[HeaderParameter, Mandatory] with MultiParameters[HSeq, MandatorySeq] {
    override def apply[T](spec: ParameterSpec[T], name: String, description: String = null) = new SingleMandatoryParameter(name, description, spec, HeaderExtractAndRebind) with HeaderParameter[T] with Mandatory[T]

    override val multi = new Parameters[HSeq, MandatorySeq] {
      override def apply[T](spec: ParameterSpec[T], name: String, description: String = null) = new MultiMandatoryParameter(name, description, spec, HeaderExtractAndRebind) with HSeq[T] with MandatorySeq[T]
    }
  }

  val optional = new Parameters[HeaderParameter, Optional] with MultiParameters[HSeq, OptionalSeq] {
    override def apply[T](spec: ParameterSpec[T], name: String, description: String = null) = new SingleOptionalParameter(name, description, spec, HeaderExtractAndRebind) with HeaderParameter[T] with Optional[T]

    override val multi = new Parameters[HSeq, OptionalSeq] {
      override def apply[T](spec: ParameterSpec[T], name: String, description: String = null) = new MultiOptionalParameter(name, description, spec, HeaderExtractAndRebind) with HSeq[T] with OptionalSeq[T]
    }

  }
}
