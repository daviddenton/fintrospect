package io.github.daviddenton.fintrospect.parameters

abstract class Parameter[T] protected[fintrospect] {
  val name: String
  val description: Option[String]
  val where: String
  val paramType: ParamType
  val requirement: Requirement
}

