package io.fintrospect.parameters

import org.jboss.netty.handler.codec.http.HttpRequest

import scala.util.Try

/**
 * Builder for parameters that are encoded as HTTP headers.
 */
object Header {

  private val aLocation = new Location {
    override def toString = "header"

    override def from(name: String, request: HttpRequest): Option[String] = Option(request.headers().get(name))
  }

  val required = new Parameters[MandatoryRequestParameter] {
    override protected def parameter[T](aName: String, aDescription: Option[String], aParamType: ParamType, parse: (String => Try[T])) =
      new MandatoryRequestParameter[T](parse) {
        val name = aName
        val location = aLocation
        val description = aDescription
        val paramType = aParamType
      }
  }

  val optional = new Parameters[OptionalRequestParameter] {
    override protected def parameter[T](aName: String, aDescription: Option[String], aParamType: ParamType, parse: (String => Try[T])) =
      new OptionalRequestParameter[T](parse) {
        val name = aName
        val location = aLocation
        val description = aDescription
        val paramType = aParamType

      }
  }
}
