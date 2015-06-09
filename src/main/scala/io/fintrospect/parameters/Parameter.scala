package io.fintrospect.parameters

trait Parameter[T] {
  val name: String
  val description: Option[String]
  val where: String
  val paramType: ParamType
  val required: Boolean

  def of(value: T): ParamBinding[T] = ->(value)

  def ->(value: T): ParamBinding[T]
}


