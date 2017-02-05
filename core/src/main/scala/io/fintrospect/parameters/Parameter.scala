package io.fintrospect.parameters

import io.fintrospect.util.ExtractionError.{Invalid, Missing}
import io.fintrospect.util.{Extracted, Extraction, ExtractionError, ExtractionFailed}

import scala.util.{Failure, Success, Try}

/**
  * A parameter is a name-value pair which can be encoded into an HTTP message. Sub-types
  * represent the various places in which values are encoded (eg. header/form/query/path)
  */
trait Parameter extends HasParameters {
  val required: Boolean
  val name: String
  val description: String
  val where: String
  val paramType: ParamType

  override def toString = s"${if (required) "Mandatory" else "Optional"} parameter $name (${paramType.name}) in $where"
}

/**
  * Parameter location specific utility functions to assist with extraction and binding of values
  */
trait ParameterExtractAndBind[From, Rep, B <: Binding] {
  def newBinding(parameter: Parameter, value: Rep): B

  def valuesFrom(parameter: Parameter, from: From): Option[Seq[Rep]]
}

trait OptionalParameter[From, T, Bnd <: Binding] extends Optional[From, T]
  with Parameter
  with Rebindable[From, T, Bnd] {
  override def <->(from: From): Iterable[Bnd] = (this <-- from).map(this.-->).getOrElse(Nil)

  /**
    * Attempt to manually deserialize from the message object, using a validation predicate and reason for failure.
    */
  def <--?(from: From, reason: String, predicate: T => Boolean): Extraction[Option[T]] =
    <--?(from) match {
      case Extracted(x) => if (x.forall(predicate)) Extraction(x) else ExtractionFailed(ExtractionError(this, reason))
      case e => e
    }

  /**
    * Attempt to manually deserialize from the message object, using a validation predicate and reason for failure.
    * User-friendly synonym for <--?(), which is why the method is final.
    */
  final def extract(from: From, reason: String, predicate: T => Boolean): Extraction[Option[T]] = <--?(from, reason, predicate)

  /**
    * This is an extra implementation of bindable to allow us to bind to Option[T] as well as [T]
    */
  def -->(value: Option[T]): Iterable[Bnd] = value.map(-->).getOrElse(Nil)
}

trait MandatoryParameter[From, T, Bnd <: Binding] extends Mandatory[From, T]
  with Parameter
  with Rebindable[From, T, Bnd] {
  override def <->(from: From): Iterable[Bnd] = this --> (this <-- from)

  /**
    * Attempt to manually deserialize from the message object, using a validation predicate and reason for failure.
    */
  def <--?(from: From, reason: String, predicate: T => Boolean): Extraction[T] =
    <--?(from) match {
      case Extracted(x) => if (predicate(x)) Extraction(x) else ExtractionFailed(ExtractionError(this, reason))
      case e => e
    }

  /**
    * Attempt to manually deserialize from the message object, using a validation predicate and reason for failure.
    * User-friendly synonym for <--?(), which is why the method is final.
    */
  final def extract(from: From, reason: String, predicate: T => Boolean): Extraction[T] = <--?(from, reason, predicate)

}

abstract class ExtractableParameter[T, From, B <: Binding, Bt, O] (spec: ParameterSpec[T], eab: ParameterExtractAndBind[From, String, B],
                                                                   bindFn: Bt => Seq[T])
  extends Parameter with Bindable[Bt, B] {

  override def iterator: Iterator[Parameter] = Seq(this).iterator

  override val paramType: ParamType = spec.paramType

  protected def extractOrHandle(from: From, tToOut: Seq[T] => O, onMissing: Extraction[O]): Extraction[O] = from match {
    case req: ExtractedRouteRequest => req.get(this)
    case _ => eab.valuesFrom(this, from)
      .map(xs => Try(xs.map(spec.deserialize)) match {
        case Success(x) => Extracted(tToOut(x))
        case Failure(_) => ExtractionFailed(Invalid(this))
      }).getOrElse(onMissing)
  }

  override def -->(value: Bt): Seq[B] = bindFn(value).map(spec.serialize).map(v => eab.newBinding(this, v))

}

abstract class SingleMandatoryParameter[T, From, B <: Binding](val name: String, val description: String, spec: ParameterSpec[T], eab: ParameterExtractAndBind[From, String, B])
  extends ExtractableParameter[T, From, B, T, T](spec, eab, (t: T) => Seq(t)) {

  def <--?(from: From): Extraction[T] = extractOrHandle(from, _.head, ExtractionFailed(Missing(this)))
}

abstract class SingleOptionalParameter[T, From, B <: Binding](val name: String, val description: String, spec: ParameterSpec[T], eab: ParameterExtractAndBind[From, String, B])
  extends ExtractableParameter[T, From, B, T, Option[T]](spec, eab, (t: T) => Seq(t)) {

  def <--?(from: From): Extraction[Option[T]] = extractOrHandle(from, _.headOption, Extracted(None))
}

abstract class MultiMandatoryParameter[T, From, B <: Binding](val name: String, val description: String, spec: ParameterSpec[T], eab: ParameterExtractAndBind[From, String, B])
  extends ExtractableParameter[T, From, B, Seq[T], Seq[T]](spec, eab, identity[Seq[T]]) {

  def <--?(from: From): Extraction[Seq[T]] = extractOrHandle(from, identity, ExtractionFailed(Missing(this)))
}

abstract class MultiOptionalParameter[T, From, B <: Binding](val name: String, val description: String, spec: ParameterSpec[T], eab: ParameterExtractAndBind[From, String, B])
  extends ExtractableParameter[T, From, B, Seq[T], Option[Seq[T]]](spec, eab, identity[Seq[T]]) {

  def <--?(from: From): Extraction[Option[Seq[T]]] = extractOrHandle(from, Some(_), Extracted(None))
}