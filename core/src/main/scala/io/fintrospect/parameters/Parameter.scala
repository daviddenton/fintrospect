package io.fintrospect.parameters

import io.fintrospect.util.ExtractionError.{Invalid, Missing}
import io.fintrospect.util.{ExtractableParameter, Extracted, Extraction, ExtractionFailed}

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

  protected def extractFrom[T](deserialize: Seq[String] => Try[T],
                               fromInput: Option[Seq[String]]): Extraction[T] =
    fromInput.map(deserialize).map {
      case Success(d) => Extracted(Some(d))
      case Failure(_) => ExtractionFailed(Invalid(this))
    }.getOrElse(if (required) ExtractionFailed(Missing(this)) else Extracted(None))
}

/**
  * Parameter location specific utility functions to assist with extraction and binding of values
  */
trait ParameterExtractAndBind[From, Rep, B <: Binding] {
  def newBinding(parameter: Parameter, value: Rep): B

  def valuesFrom(parameter: Parameter, from: From): Option[Seq[Rep]]
}

trait OptionalParameter[From, T, Bnd <: Binding] extends Optional[From, T]
  with ExtractableParameter[From, T]
  with Rebindable[From, T, Bnd] {
  override def <->(from: From): Iterable[Bnd] = (this <-- from).map(this.-->).getOrElse(Nil)

  /**
    * This is an extra implementation of bindable to allow us to bind to Option[T] as well as [T]
    */
  def -->(value: Option[T]): Iterable[Bnd] = value.map(-->).getOrElse(Nil)
}

trait MandatoryParameter[From, T, Bnd <: Binding] extends Mandatory[From, T]
  with ExtractableParameter[From, T]
  with Rebindable[From, T, Bnd] {
  override def <->(from: From): Iterable[Bnd] = this --> (this <-- from)
}

abstract class SingleParameter[T, From, B <: Binding](val name: String, val description: String, spec: ParameterSpec[T], eab: ParameterExtractAndBind[From, String, B])
  extends Parameter with Bindable[T, B] {

  override def iterator: Iterator[Parameter] = Seq(this).iterator

  override val paramType = spec.paramType

  override def -->(value: T) = Seq(eab.newBinding(this, spec.serialize(value)))

  def <--?(from: From): Extraction[T] = from match {
    case req: ExtractedRouteRequest => req.get(this)
    case _ => extractFrom(xs => Try(spec.deserialize(xs.head)), eab.valuesFrom(this, from))
  }
}

abstract class MultiParameter[T, From, B <: Binding](val name: String, val description: String, spec: ParameterSpec[T], eab: ParameterExtractAndBind[From, String, B])
  extends Parameter with Bindable[Seq[T], B] {
  override val paramType = spec.paramType

  override def iterator: Iterator[Parameter] = Seq(this).iterator

  override def -->(value: Seq[T]) = value.map(v => eab.newBinding(this, spec.serialize(v)))

  def <--?(from: From): Extraction[Seq[T]] = from match {
    case req: ExtractedRouteRequest => req.get(this)
    case _ => extractFrom(xs => Try(xs.map(spec.deserialize)), eab.valuesFrom(this, from))
  }
}


