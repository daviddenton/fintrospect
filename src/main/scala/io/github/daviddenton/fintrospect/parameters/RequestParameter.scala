package io.github.daviddenton.fintrospect.parameters

import scala.reflect.ClassTag

abstract class RequestParameter[T](name: String, description: Option[String], location: Location, parse: (String => Option[T]))(implicit ct: ClassTag[T])
  extends Parameter[T](name, description, location.toString)(ct) {
}
