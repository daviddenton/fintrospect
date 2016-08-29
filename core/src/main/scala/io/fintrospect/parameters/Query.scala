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


  val required = new Parameters[QueryParameter, Mandatory] with MultiParameters[MultiQueryParameter, MandatorySeq] {
    override def apply[T](spec: ParameterSpec[T]) = new SingleParameter(spec, QueryExtractAndRebind) with QueryParameter[T] with Mandatory[T]

    override val multi = new Parameters[MultiQueryParameter, MandatorySeq] {
      override def apply[T](spec: ParameterSpec[T]) = new MultiQueryParameter(spec) with MandatorySeq[T]
    }
  }

  val optional = new Parameters[QueryParameter, Optional] with MultiParameters[MultiQueryParameter, OptionalSeq] {
    override def apply[T](spec: ParameterSpec[T]) = new SingleParameter(spec, QueryExtractAndRebind) with QueryParameter[T] with Optional[T]

    override val multi = new Parameters[MultiQueryParameter, OptionalSeq] {
      override def apply[T](spec: ParameterSpec[T]) = new MultiQueryParameter(spec) with OptionalSeq[T]
    }
  }
}
