package io.github.daviddenton.fintrospect.parameters

import scala.reflect.ClassTag

object Path extends Parameters[PathParameter]() {
  protected def create[T](name: String, description: Option[String], parse: (String => Option[T]))(implicit ct: ClassTag[T]) = new PathParameter[T](name, description)(ct) {

    override def toString() = s"{$name}"

    override def unapply(str: String): Option[T] = parse(str)

    override def iterator: Iterator[PathParameter[_]] = Some(this).iterator
  }

  def fixed(name: String): PathParameter[String] = new PathParameter[String](name, None) {

    override def toString() = name

    override def unapply(str: String): Option[String] = if (str == name) Some(str) else None

    override def iterator: Iterator[PathParameter[_]] = Nil.iterator
  }
}
