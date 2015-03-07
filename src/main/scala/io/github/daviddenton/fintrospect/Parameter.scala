package io.github.daviddenton.fintrospect

import java.beans.Introspector._

import scala.reflect.ClassTag

class Parameter[T] protected[fintrospect](val name: String, val location: String, val required: Boolean)(implicit ct: ClassTag[T]) {
  val paramType = decapitalize(ct.runtimeClass.getSimpleName)
}

abstract class RequestParameter[T](name: String, location: String, required: Boolean)(implicit ct: ClassTag[T]) extends Parameter[T](name, location, required)(ct) {
  
}