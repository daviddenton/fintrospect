package io.github.daviddenton.fintrospect

import java.beans.Introspector._

import com.twitter.finagle.http.Request

import scala.reflect.ClassTag

abstract class Parameter[T] protected[fintrospect](val name: String, val where: String, val required: Boolean)(implicit ct: ClassTag[T]) {
  val paramType = decapitalize(ct.runtimeClass.getSimpleName)
}

class PathParameter[T](name: String, parse: (String => Option[T]))(implicit ct: ClassTag[T]) extends Parameter[T](name, "path", true)(ct) with SegmentMatcher[T] {
  override val toParameter: Option[Parameter[_]] = Some(this)
  def unapply(str: String): Option[T] = parse(str)
  override def toString = s"{$name}"
}

class RequestParameter[T](name: String, location: Location, required: Boolean, parse: (String => Option[T]))(implicit ct: ClassTag[T]) extends Parameter[T](name, location.toString, required)(ct) {
  def from(request: Request): Option[T] = location.from(name, request).flatMap(parse)
  def unapply(str: String): Option[T] = parse(str)
}