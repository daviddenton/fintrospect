package io.fintrospect.parameters

import com.twitter.finagle.http.Request
import io.fintrospect.parameters.InvalidParameter.Missing

/**
  * Parameters which are bound to the query part of a URL
  */
object Query {

  trait Mandatory[T] extends io.fintrospect.parameters.Mandatory[T, Request] with MandatoryRebind[T, Request, QueryBinding] {
    self: Bindable[T, QueryBinding] =>
  }

  trait MandatorySeq[T] extends io.fintrospect.parameters.Mandatory[Seq[T], Request] with MandatoryRebind[Seq[T], Request, QueryBinding] {
    self: Bindable[Seq[T], QueryBinding] =>
  }

  trait Optional[T] extends io.fintrospect.parameters.Optional[T, Request]
  with OptionalBindable[T, QueryBinding]
  with OptionalRebind[T, Request, QueryBinding] {
    self: Bindable[T, QueryBinding] =>
  }

  trait OptionalSeq[T] extends io.fintrospect.parameters.Optional[Seq[T], Request]
  with OptionalBindable[Seq[T], QueryBinding]
  with OptionalRebind[Seq[T], Request, QueryBinding] {
    self: Bindable[Seq[T], QueryBinding] =>
  }

  val required = new Parameters[QueryParameter, Mandatory] with MultiParameters[MultiQueryParameter, MandatorySeq] {
    override def apply[T](spec: ParameterSpec[T]) = new SingleQueryParameter[T](spec) with Mandatory[T] {
      override def <--?(request: Request) = get[T](request, identity, ExtractionFailed(Missing(this)))
    }

    override val multi = new Parameters[MultiQueryParameter, MandatorySeq] {
      override def apply[T](spec: ParameterSpec[T]) = new MultiQueryParameter[T](spec) with MandatorySeq[T] {
        override def <--?(request: Request) = get[Seq[T]](request, identity, ExtractionFailed(Missing(this)))
      }
    }
  }

  val optional = new Parameters[QueryParameter, Optional] with MultiParameters[MultiQueryParameter, OptionalSeq] {
    override def apply[T](spec: ParameterSpec[T]) = new SingleQueryParameter[T](spec) with Optional[T] {
      override def <--?(request: Request) = get[Option[T]](request, Some(_), NotProvided())
    }

    override val multi = new Parameters[MultiQueryParameter, OptionalSeq] {
      override def apply[T](spec: ParameterSpec[T]) = new MultiQueryParameter[T](spec) with OptionalSeq[T] {
        override def <--?(request: Request) = get[Option[Seq[T]]](request, Some(_), NotProvided())
      }
    }
  }
}
