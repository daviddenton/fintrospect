package io.github.daviddenton.fintrospect.parameters

import scala.reflect.ClassTag

object Path extends Parameters[PathParameter]() {
  protected def create[T](name: String, required: Boolean, parse: (String => Option[T]))(implicit ct: ClassTag[T]) = new PathParameter[T](name)(ct) {
    def unapply(str: String): Option[T] = parse(str)

    override def toString() = s"{$name}"

    override def iterator: Iterator[PathParameter[_]] = Some(this).iterator
  }

  def fixed(value: String): PathParameter[String] = new PathParameter[String](value) {

    override def toString() = name

    override def unapply(str: String): Option[String] = if (str == value) Some(str) else None

    override def iterator: Iterator[PathParameter[_]] = Nil.iterator
  }
}
