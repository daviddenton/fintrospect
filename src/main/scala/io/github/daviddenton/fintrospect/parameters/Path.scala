package io.github.daviddenton.fintrospect.parameters

import scala.reflect.ClassTag

object Path extends Parameters[PathParameter]() {
  protected def create[T](name: String, required: Boolean, parse: (String => Option[T]))(implicit ct: ClassTag[T]) = new PathParameter[T](name, parse)

  def fixed(value: String): SegmentMatcher[String] = new SegmentMatcher[String] {
    val toParameter: Option[Parameter[_]] = None

    override def unapply(str: String): Option[String] = if (str == value) Some(str) else None

    override def toString: String = value
  }

}
