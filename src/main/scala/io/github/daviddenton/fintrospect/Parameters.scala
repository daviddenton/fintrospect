package io.github.daviddenton.fintrospect

import scala.reflect.ClassTag

object Parameters {
  def path[T](name: String)(implicit ct: ClassTag[T]) = new Parameter[T](name, "path", true)

  def body[T](name: String)(implicit ct: ClassTag[T]) = new RequestParameter[T](name, "body", true) {}

  def query[T](name: String)(implicit ct: ClassTag[T]) = new RequestParameter[T](name, "query", true) {}

  def header[T](name: String)(implicit ct: ClassTag[T]) = new RequestParameter[T](name, "header", true) {}
}
