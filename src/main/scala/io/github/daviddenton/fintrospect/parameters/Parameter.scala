package io.github.daviddenton.fintrospect.parameters

trait Parameter[T] {
  val name: String
  val description: Option[String]
  val where: String
  val paramType: ParamType
  val requirement: Requirement
}

