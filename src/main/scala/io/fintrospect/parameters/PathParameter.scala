package io.fintrospect.parameters

import java.net.URI

import org.jboss.netty.handler.codec.http.HttpRequest

import scala.util.Try

abstract class PathParameter[T]() extends Parameter[T] with Iterable[PathParameter[_]] {
  override val where = "path"
  override val requirement = Requirement.Mandatory

  def unapply(str: String): Option[T]
}

object PathParameter {
  val builder = () => new ParameterBuilder[PathParameter, Mandatory]() {
    override def apply[T](aName: String,
                          aDescription: Option[String],
                          aParamType: ParamType,
                          parse: (String => Try[T]))
    = new PathParameter[T] with Mandatory[T] {
      override val name = aName
      override val description = aDescription
      override val paramType = aParamType

      override def toString() = s"{$name}"

      override def unapply(str: String): Option[T] = Option(str).flatMap(s => {
        parse(new URI("http://localhost/" + s).getPath.substring(1)).toOption
      })

      override def iterator: Iterator[PathParameter[_]] = Some(this).iterator

      override def parseFrom(request: HttpRequest): Option[Try[T]] = ???
    }
  }
}
