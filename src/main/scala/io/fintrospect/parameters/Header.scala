package io.fintrospect.parameters

import com.twitter.finagle.http.Message
import io.fintrospect.parameters.InvalidParameter.{Invalid, Missing}

import scala.util.{Failure, Success, Try}

/**
  * Parameters which are bound to request/response headers
  */
object Header {

  trait Mandatory[T] extends io.fintrospect.parameters.Mandatory[T, Message]
  with MandatoryRebind[T, Message, RequestBinding] {
    self: Bindable[T, RequestBinding] =>
  }

  trait MandatorySeq[T] extends io.fintrospect.parameters.Mandatory[Seq[T], Message] with MandatoryRebind[Seq[T], Message, RequestBinding] {
    self: Bindable[Seq[T], RequestBinding] =>
  }

  trait Optional[T] extends io.fintrospect.parameters.Optional[T, Message]
  with OptionalBindable[T, RequestBinding]
  with OptionalRebind[T, Message, RequestBinding] {
    self: Bindable[T, RequestBinding] =>
  }

  trait OptionalSeq[T] extends io.fintrospect.parameters.Optional[Seq[T], Message]
  with OptionalBindable[Seq[T], RequestBinding]
  with OptionalRebind[Seq[T], Message, RequestBinding] {
    self: Bindable[Seq[T], RequestBinding] =>
  }

  private def get[I, O](param: HeaderParameter[I], message: Message, fn: I => O, default: Extraction[O]): Extraction[O] = {
    val headers = message.headerMap.getAll(param.name)
    val opt = if (headers.isEmpty) None else Some(headers.toSeq)
    opt.map(v => Try(param.deserialize(v)) match {
      case Success(d) => Extracted(fn(d))
      case Failure(_) => ExtractionFailed(Invalid(param))
    }).getOrElse(default)
  }

  val required = new Parameters[HeaderParameter, Mandatory] with MultiParameters[MultiHeaderParameter, MandatorySeq] {
    override def apply[T](spec: ParameterSpec[T]) = new SingleHeaderParameter[T](spec)
      with Mandatory[T] {
      override def <--?(message: Message) = get[T, T](this, message, identity, ExtractionFailed(Missing(this)))
    }

    override val multi = new Parameters[MultiHeaderParameter, MandatorySeq] {
      override def apply[T](spec: ParameterSpec[T]) = new MultiHeaderParameter[T](spec) with MandatorySeq[T] {
        override def <--?(message: Message) = get[Seq[T], Seq[T]](this, message, identity, ExtractionFailed(Missing(this)))
      }
    }
  }

  val optional = new Parameters[HeaderParameter, Optional] with MultiParameters[MultiHeaderParameter, OptionalSeq] {
    override def apply[T](spec: ParameterSpec[T]) = new SingleHeaderParameter[T](spec)
      with Optional[T] {
      override def <--?(message: Message) = get[T, Option[T]](this, message, Some(_), NotProvided())
    }

    override val multi = new Parameters[MultiHeaderParameter, OptionalSeq] {
      override def apply[T](spec: ParameterSpec[T]) = new MultiHeaderParameter[T](spec) with OptionalSeq[T] {
        override def <--?(message: Message) = get[Seq[T], Option[Seq[T]]](this, message, Some(_), NotProvided())
      }
    }

  }
}
