package io.github.daviddenton.fintrospect.parameters

abstract class PathParameter[T](name: String, description: Option[String], paramType: ParamType)
  extends Parameter[T](name, description, "path", paramType)
  with Iterable[PathParameter[_]] {
  def unapply(str: String): Option[T]
}

object PathParameter {
  val builder = new ParameterBuilder[PathParameter]() {

    override def apply[T](name: String, description: Option[String], paramType: ParamType, parse: (String => Option[T])) = new PathParameter[T](name, description, paramType) {
      override def toString() = s"{$name}"

      override def unapply(str: String): Option[T] = parse(str)

      override def iterator: Iterator[PathParameter[_]] = Some(this).iterator
    }
  }
}