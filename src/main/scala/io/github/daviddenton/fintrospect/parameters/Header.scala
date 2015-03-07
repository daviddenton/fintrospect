package io.github.daviddenton.fintrospect.parameters

import Locations.HeaderLocation

import scala.reflect.ClassTag

object Header extends Parameters[RequestParameter]() {
  protected def create[T](name: String, required: Boolean, parse: (String => Option[T]))(implicit ct: ClassTag[T]) = new RequestParameter[T](name, HeaderLocation, required, parse)
}
