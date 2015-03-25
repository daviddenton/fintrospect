package io.github.daviddenton.fintrospect.parameters

import scala.reflect.ClassTag

trait ParameterBuilder[P[_]] {
  def apply[T](name: String, description: Option[String], parse: (String => Option[T]))(implicit ct: ClassTag[T]): P[T]
}
