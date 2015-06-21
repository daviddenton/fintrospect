package io.fintrospect.parameters

trait Parameter[T] extends Bindable[T] {
  val required: Boolean
  val name: String
  val description: Option[String]
  val where: String
  val paramType: ParamType

  override def toString = s"Parameter(name=$name,where=$where,paramType=${paramType.name})"
}


