package io.github.daviddenton.fintrospect.parameters

import com.twitter.finagle.http.Request

abstract class RequestParameter[T](name: String,
                                   description: Option[String],
                                   location: Location,
                                   paramType: ParamType,
                                   parse: (String => Option[T]))
  extends Parameter[T](name, description, location.toString, paramType) {
  def unapply(request: Request): Option[T] = location.from(name, request).flatMap(parse)
}
