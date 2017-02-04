package io.fintrospect.parameters

import com.twitter.finagle.http.Request

/**
  * Parameters which are bound to the query part of a URL
  */
object Query {

  type Mandatory[T] = MandatoryParameter[Request, T, QueryBinding]

  type MandatorySeq[T] = MandatoryParameter[Request, Seq[T], QueryBinding]

  type Optional[T] = OptionalParameter[Request, T, QueryBinding]

  type OptionalSeq[T] = OptionalParameter[Request, Seq[T], QueryBinding]


  val required = new Parameters[QueryParameter, Mandatory] with MultiParameters[MultiMandatoryQueryParameter, MandatorySeq] {
    override def apply[T](spec: ParameterSpec[T], name: String, description: String = null) = new SingleMandatoryParameter(name, description, spec, QueryExtractAndRebind) with QueryParameter[T] with Mandatory[T]

    override val multi = new Parameters[MultiMandatoryQueryParameter, MandatorySeq] {
      override def apply[T](spec: ParameterSpec[T], name: String, description: String = null) = new MultiMandatoryQueryParameter(name, description, spec) with MandatorySeq[T]
    }
  }

  val optional = new Parameters[QueryParameter, Optional] with MultiParameters[MultiOptionalQueryParameter, OptionalSeq] {
    override def apply[T](spec: ParameterSpec[T], name: String, description: String = null) = new SingleOptionalParameter(name, description, spec, QueryExtractAndRebind) with QueryParameter[T] with Optional[T]

    override val multi = new Parameters[MultiOptionalQueryParameter, OptionalSeq] {
      override def apply[T](spec: ParameterSpec[T], name: String, description: String = null) = new MultiOptionalQueryParameter(name, description, spec) with OptionalSeq[T]
    }
  }
}
