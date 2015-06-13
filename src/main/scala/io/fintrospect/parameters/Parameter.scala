package io.fintrospect.parameters

trait Parameter[T] {
  val required: Boolean
  val name: String
  val description: Option[String]
  val where: String
  val paramType: ParamType

  def of(value: T): ParamBinding[T] = ->(value)

  def ->(value: T): ParamBinding[T]

  override def toString: String = s"Parameter(name=$name,where=$where,paramType=${paramType.name})"
}


