package io.github.daviddenton.fintrospect

import java.beans.Introspector._

import scala.reflect.ClassTag

class Parameter[T] private(val name: String, val location: String, val required: Boolean)(implicit ct: ClassTag[T]) {
  val paramType = decapitalize(ct.runtimeClass.getSimpleName)
}

object Parameter {
  def path[T](name: String)(implicit ct: ClassTag[T]) = new Parameter[T](name, "path", true)
  def body[T](name: String)(implicit ct: ClassTag[T]) = new Parameter[T](name, "body", true)
  def query[T](name: String)(implicit ct: ClassTag[T]) = new Parameter[T](name, "query", true)
  def header[T](name: String)(implicit ct: ClassTag[T]) = new Parameter[T](name, "header", true)
}