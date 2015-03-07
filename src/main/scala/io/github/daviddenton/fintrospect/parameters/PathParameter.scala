package io.github.daviddenton.fintrospect.parameters

import scala.reflect.ClassTag

abstract class PathParameter[T](name: String)(implicit ct: ClassTag[T]) extends Parameter[T](name, "path", true)(ct) {
  val toParameter: Option[PathParameter[_]]
  def unapply(str: String): Option[T]
}
