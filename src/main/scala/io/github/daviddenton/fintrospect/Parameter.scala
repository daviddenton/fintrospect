package io.github.daviddenton.fintrospect

import java.beans.Introspector._

import com.twitter.finagle.http.Request

import scala.reflect.ClassTag

class Parameter[T] protected[fintrospect](val name: String, val where: String, val required: Boolean)(implicit ct: ClassTag[T]) {
  val paramType = decapitalize(ct.runtimeClass.getSimpleName)
}

abstract class RequestParameter[T](name: String, location: Location, required: Boolean)(implicit ct: ClassTag[T]) extends Parameter[T](name, location.toString, required)(ct) {
  def from(request: Request): Option[T] = location.from(name, request).flatMap(unapply)
  def unapply(str: String): Option[T]
}