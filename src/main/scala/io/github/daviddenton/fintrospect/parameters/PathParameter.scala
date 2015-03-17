package io.github.daviddenton.fintrospect.parameters

import scala.reflect.ClassTag

abstract class PathParameter[T](name: String, description: Option[String])(implicit ct: ClassTag[T])
  extends Parameter[T](name, description, "path")(ct)
  with Iterable[PathParameter[_]] {
  def unapply(str: String): Option[T]
}
