package io.github.daviddenton.fintrospect

import java.beans.Introspector._

import scala.reflect.ClassTag

class Parameter[T] protected[fintrospect](val name: String, val location: String, val required: Boolean)(implicit ct: ClassTag[T]) {
  val paramType = decapitalize(ct.runtimeClass.getSimpleName)
}
