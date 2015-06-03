package io.fintrospect.parameters

import java.net.URI

import scala.util.Try

abstract class PathParameter[T]() extends Parameter[T] with Iterable[PathParameter[_]] {
  override val where = "path"
  override val requirement = Requirement.Mandatory

  def unapply(str: String): Option[T]
  def apply(t: T): String = ???
}

object PathParameter {
  val builder = new ParameterBuilder[PathParameter]() {
    override def apply[T](aName: String,
                          aDescription: Option[String],
                          aParamType: ParamType,
                          parse: (String => Try[T])) = new PathParameter[T] {
      override val name = aName
      override val description = aDescription
      override val paramType = aParamType

      override def toString() = s"{$name}"

      override def unapply(str: String): Option[T] = Option(str).flatMap(s => { parse(new URI("http://localhost/" + s).getPath.substring(1)).toOption})

      override def iterator: Iterator[PathParameter[_]] = Some(this).iterator
    }
  }
}
