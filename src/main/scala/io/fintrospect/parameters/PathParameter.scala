package io.fintrospect.parameters

import org.jboss.netty.handler.codec.http.HttpRequest

abstract class PathParameter[T](val name: String, val description: Option[String], val paramType: ParamType) extends Parameter[T] with Iterable[PathParameter[_]] {
  override val location = new Location {
    override def toString = "path"
    override def from(name: String, request: HttpRequest): Option[String] = ???
  }

  def unapply(str: String): Option[T]
}
