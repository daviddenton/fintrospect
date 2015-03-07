package io.github.daviddenton.fintrospect.parameters

import com.twitter.finagle.http.Request

import scala.reflect.ClassTag

class RequestParameter[T](name: String, location: Location, required: Requirement, parse: (String => Option[T]))(implicit ct: ClassTag[T]) extends Parameter[T](name, location.toString, required)(ct) {
  def from(request: Request): Option[T] = location.from(name, request).flatMap(parse)

  def unapply(str: String): Option[T] = parse(str)
}
