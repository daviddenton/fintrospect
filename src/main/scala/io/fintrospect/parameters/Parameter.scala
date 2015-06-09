package io.fintrospect.parameters

trait Parameter[T] {
  val name: String
  val description: Option[String]
  val where: String
  val paramType: ParamType
  val required: Boolean
  def apply(value: T): String
}

