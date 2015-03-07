package io.github.daviddenton.fintrospect.parameters

import java.beans.Introspector._

import scala.reflect.ClassTag

abstract class Parameter[T] protected[fintrospect](val name: String, val where: String, val required: Boolean)(implicit ct: ClassTag[T]) {
  val paramType = decapitalize(ct.runtimeClass.getSimpleName)
}



