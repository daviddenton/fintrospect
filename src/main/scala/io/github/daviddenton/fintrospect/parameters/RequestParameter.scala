package io.github.daviddenton.fintrospect.parameters

import io.github.daviddenton.fintrospect.FinagleTypeAliases.FTRequest

abstract class RequestParameter[T](name: String,
                                   description: Option[String],
                                   location: Location,
                                   paramType: ParamType,
                                   parse: (String => Option[T]))
  extends Parameter[T](name, description, location.toString, paramType) {
  def unapply(request: FTRequest): Option[T] = location.from(name, request).flatMap(parse)
}
