package io.github.daviddenton.fintrospect.parameters

import com.twitter.finagle.http.Request

import scala.reflect.ClassTag

class RequiredRequestParameter[T](name: String, description: Option[String], location: Location, parse: (String => Option[T]))(implicit ct: ClassTag[T])
  extends RequestParameter[T](name, description, location, parse)(ct) {
  def from(request: Request): T = unapply(request).get

  def unapply(request: Request): Option[T] = location.from(name, request).flatMap(parse)
}

object RequiredRequestParameter {
  def builderFor(location: Location) = new ParameterBuilder[RequiredRequestParameter]() {
    def apply[T](name: String, description: Option[String], parse: (String => Option[T]))(implicit ct: ClassTag[T]) = new RequiredRequestParameter[T](name, description, location, parse)
  }
}