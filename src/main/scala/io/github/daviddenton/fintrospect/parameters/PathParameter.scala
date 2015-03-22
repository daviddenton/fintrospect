package io.github.daviddenton.fintrospect.parameters

import scala.reflect.ClassTag

abstract class PathParameter[T](name: String, description: Option[String])(implicit ct: ClassTag[T])
  extends Parameter[T](name, description, "path")(ct)
  with Iterable[PathParameter[_]] {
  def unapply(str: String): Option[T]
}

object PathParameter {
  val builder = new ParameterBuilder[PathParameter]() {
    def apply[T](name: String, description: Option[String], parse: (String => Option[T]))(implicit ct: ClassTag[T]) = new PathParameter[T](name, description)(ct) {

      override def toString() = s"{$name}"

      override def unapply(str: String): Option[T] = parse(str)

      override def iterator: Iterator[PathParameter[_]] = Some(this).iterator
    }
  }
}