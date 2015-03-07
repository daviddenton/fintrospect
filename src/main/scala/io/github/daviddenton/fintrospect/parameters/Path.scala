package io.github.daviddenton.fintrospect.parameters

import io.github.daviddenton.fintrospect.parameters.PathParameter

import scala.reflect.ClassTag

object Path extends Parameters[PathParameter]() {
  protected def create[T](name: String, required: Boolean, parse: (String => Option[T]))(implicit ct: ClassTag[T]) = new PathParameter[T](name, parse)
}
