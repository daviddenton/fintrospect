package io.fintrospect.parameters

import java.net.URI

import org.jboss.netty.handler.codec.http.HttpRequest

import scala.util.Try

/**
 * Builder for parameters that are encoded in the HTTP request path.
 */
object Path extends Parameters[PathParameter, Mandatory] {

  /**
   * A special path segment that is defined, but has no intrinsic value other than for route matching. Useful when embedded
   * between 2 other path parameters. eg. /myRoute/{id}/aFixedPart/{subId}
   */
  def fixed(name: String): PathParameter[String] = new PathParameter[String](name, None, StringParamType) {

    override val required = true

    override def toString() = name

    override def apply(value: String): String = value

    override def unapply(str: String): Option[String] = if (str == name) Some(str) else None

    override def iterator: Iterator[PathParameter[_]] = Nil.iterator
  }

  override protected def parameter[T](name: String,
                                      description: Option[String],
                                      paramType: ParamType,
                                      parse: String => T,
                                      format: T => String)
  = new PathParameter[T](name, description, paramType) with Mandatory[T] {

    override def toString() = s"{$name}"

    override def apply(value: T): String = format(value)

    override def unapply(str: String): Option[T] = Option(str).flatMap(s => {
      Try(parse(new URI("http://localhost/" + s).getPath.substring(1))).toOption
    })

    override def iterator: Iterator[PathParameter[_]] = Some(this).iterator

    override def from(request: HttpRequest): T = ???
  }
}
