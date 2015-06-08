package io.fintrospect.parameters

trait Parameter[T] {
  val name: String
  val description: Option[String]
  val location: Location
  val paramType: ParamType
  val required: Boolean
}

