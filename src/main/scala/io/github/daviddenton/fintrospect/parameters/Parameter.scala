package io.github.daviddenton.fintrospect.parameters

import java.beans.Introspector._

import scala.reflect.ClassTag

abstract class Parameter[T] protected[fintrospect](val name: String, val description: Option[String], val where: String)(implicit ct: ClassTag[T]) {
  val paramType = decapitalize(ct.runtimeClass.getSimpleName)
}


