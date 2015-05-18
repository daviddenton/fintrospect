package io.github.daviddenton.fintrospect.parameters

import java.net.URI

import scala.util.Try

abstract class PathParameter[T](val name: String, val description: Option[String], val paramType: ParamType) extends Parameter[T] with Iterable[PathParameter[_]] {
  override val where = "path"
  override val requirement = Requirement.Mandatory

  def unapply(str: String): Option[T]
}

object PathParameter {
  val builder = new ParameterBuilder[PathParameter]() {
    override def apply[T](name: String, description: Option[String], paramType: ParamType, parse: (String => Option[T])) = new PathParameter[T](name, description, paramType) {
      override def toString() = s"{$name}"

      override def unapply(str: String): Option[T] = {
        Try(new URI("http://localhost/" + str).getPath.substring(1)).toOption.flatMap(parse)
      }

      override def iterator: Iterator[PathParameter[_]] = Some(this).iterator
    }
  }
}
