package io.github.daviddenton.fintrospect.parameters

trait ParameterBuilder[P[_]] {
  def apply[T](name: String, description: Option[String], paramType: ParamType, parse: (String => Option[T])): P[T]
}
