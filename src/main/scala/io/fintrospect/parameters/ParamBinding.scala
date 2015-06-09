package io.fintrospect.parameters

case class ParamBinding[T](parameter: Parameter[T], value: String)
