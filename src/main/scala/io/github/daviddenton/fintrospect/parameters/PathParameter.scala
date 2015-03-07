package io.github.daviddenton.fintrospect.parameters

import io.github.daviddenton.fintrospect.SegmentMatcher

import scala.reflect.ClassTag

class PathParameter[T](name: String, parse: (String => Option[T]))(implicit ct: ClassTag[T]) extends Parameter[T](name, "path", true)(ct) with SegmentMatcher[T] {
   override val toParameter: Option[Parameter[_]] = Some(this)
   def unapply(str: String): Option[T] = parse(str)
   override def toString = s"{$name}"
 }
