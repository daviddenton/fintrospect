package io.fintrospect.parameters

import io.fintrospect.util.HttpRequestResponseUtil._
import org.jboss.netty.handler.codec.http.{HttpRequest, QueryStringDecoder}

import scala.util.Try

/**
 * Builder for parameters that are encoded in the HTTP form.
 */
object Form {
  private val aLocation = new Location {
    override def toString = "form"

    override def from(name: String, request: HttpRequest): Option[String] = {
      Try(new QueryStringDecoder("?" + contentFrom(request)).getParameters.get(name).get(0)).toOption
    }
  }

  val required = new Parameters[RequestParameter, Mandatory] {
    override protected def parameter[T](aName: String, aDescription: Option[String], aParamType: ParamType, parse: (String => Try[T])) =
      new RequestParameter[T](parse) with Mandatory[T] {
        val name = aName
        val location = aLocation
        val description = aDescription
        val paramType = aParamType
      }
  }

  val optional = new Parameters[RequestParameter, Optional] {
    override protected def parameter[T](aName: String, aDescription: Option[String], aParamType: ParamType, parse: (String => Try[T])) =
      new RequestParameter[T](parse) with Optional[T] {
        val name = aName
        val location = aLocation
        val description = aDescription
        val paramType = aParamType
      }
  }
}
