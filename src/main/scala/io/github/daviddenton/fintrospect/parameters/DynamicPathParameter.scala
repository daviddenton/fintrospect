package io.github.daviddenton.fintrospect.parameters

import scala.reflect.ClassTag

class DynamicPathParameter[T](name: String, parse: (String => Option[T]))(implicit ct: ClassTag[T]) extends PathParameter[T](name)(ct) {
  def unapply(str: String): Option[T] = parse(str)
  override def toString = s"{$name}"
  override val toParameter: Option[PathParameter[_]] = Some(this)
}
