package io.github.daviddenton.fintrospect.parameters

import io.github.daviddenton.fintrospect.parameters.Locations.QueryLocation

import scala.reflect.ClassTag

object Query extends Parameters[RequestParameter]() {
  protected def create[T](name: String, required: Boolean, parse: (String => Option[T]))(implicit ct: ClassTag[T]) = new RequestParameter[T](name, QueryLocation, required, parse)
}
