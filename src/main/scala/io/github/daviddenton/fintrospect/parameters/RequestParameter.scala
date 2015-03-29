package io.github.daviddenton.fintrospect.parameters

abstract class RequestParameter[T](name: String, description: Option[String], location: Location, paramType: ParamType, parse: (String => Option[T]))
  extends Parameter[T](name, description, location.toString, paramType) {
}
