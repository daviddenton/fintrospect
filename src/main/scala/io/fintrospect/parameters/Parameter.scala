package io.fintrospect.parameters

/**
  * A parameter is a name-value pair which can be encoded into an HTTP message. Sub-types
  * represent the various places in which values are encoded (eg. header/form/query/path)
  */
trait Parameter {
  val required: Boolean
  val name: String
  val description: Option[String]
  val where: String
  val paramType: ParamType

  override def toString = s"Parameter(name=$name,where=$where,paramType=${paramType.name})"
}

abstract class SingleParameter[T, From, B <: Binding](spec: ParameterSpec[T], eab: ExtractAndRebind[From, B]) {
  self: Parameter with Bindable[T, B] =>

  override val name = spec.name
  override val description = spec.description
  override val paramType = spec.paramType

  override def -->(value: T) = Seq(eab.newBinding(this, spec.serialize(value)))

  protected def extract(from: From) = Extraction(this, xs => spec.deserialize(xs.head), eab.valuesFrom(this, from))
}

abstract class MultiParameter[T, From, B <: Binding](spec: ParameterSpec[T], eab: ExtractAndRebind[From, B]) {

  self: Parameter with Bindable[Seq[T], B] =>

  override val name = spec.name
  override val description = spec.description
  override val paramType = spec.paramType

  override def -->(value: Seq[T]) = value.map(v => eab.newBinding(this, spec.serialize(v)))

  protected def extract(from: From) = Extraction(this, xs => xs.map(spec.deserialize), eab.valuesFrom(this, from))
}
