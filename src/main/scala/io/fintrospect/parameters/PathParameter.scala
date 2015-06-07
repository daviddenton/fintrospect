package io.fintrospect.parameters

import java.net.URI

import org.jboss.netty.handler.codec.http.HttpRequest

import scala.util.Try

abstract class PathParameter[T](val name: String, val description: Option[String], val paramType: ParamType) extends Parameter[T] with Iterable[PathParameter[_]] {
  override val where = "path"
  override val requirement = Requirement.Mandatory

  def unapply(str: String): Option[T]
}

object PathParameter {
  def builder[T](name: String, description: Option[String],
                 paramType: ParamType,
                 parse: (String => Try[T]))
  = new PathParameter[T](name, description, paramType) with Mandatory[T] {

    override def toString() = s"{$name}"

    override def unapply(str: String): Option[T] = Option(str).flatMap(s => {
      parse(new URI("http://localhost/" + s).getPath.substring(1)).toOption
    })

    override def iterator: Iterator[PathParameter[_]] = Some(this).iterator

    override def parseFrom(request: HttpRequest): Option[Try[T]] = ???
  }

}
