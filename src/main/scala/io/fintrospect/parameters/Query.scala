package io.fintrospect.parameters

import com.twitter.finagle.http.Request
import io.fintrospect.parameters.types._

/**
  * Parameters which are bound to the query part of a URL
  */
object Query {

  trait Mandatory[T] extends Mand[Request, T, QueryBinding] {
    self: Param[Request, T, QueryBinding] =>
  }

  trait MandatorySeq[T] extends Mand[Request, Seq[T], QueryBinding] {
    self: Param[Request, Seq[T], QueryBinding] =>
  }

  trait Optional[T] extends Opt[Request, T, QueryBinding] {
    self: Param[Request, T, QueryBinding] =>
  }

  trait OptionalSeq[T] extends Opt[Request, Seq[T], QueryBinding] {
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
