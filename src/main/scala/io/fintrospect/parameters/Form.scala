package io.fintrospect.parameters

import io.fintrospect.util.HttpRequestResponseUtil._
import org.jboss.netty.handler.codec.http.{HttpRequest, QueryStringDecoder}

import scala.util.Try

/**
 * Builder for parameters that are encoded in the HTTP form.
 */
object Form {
  private val location = new Location {
    override def toString = "form"

    override def from(name: String, request: HttpRequest): Option[String] = {
      Try(new QueryStringDecoder("?" + contentFrom(request)).getParameters.get(name).get(0)).toOption
    }
  }

  val required = new Parameters[NonBodyRequestParameter, Mandatory] {
    override protected def parameter[T](name: String, description: Option[String], paramType: ParamType, parse: (String => Try[T])) =
      new NonBodyRequestParameter[T](name, description, paramType, location, parse) with Mandatory[T]
  }

  val optional = new Parameters[NonBodyRequestParameter, Optional] {
    override protected def parameter[T](name: String, description: Option[String], paramType: ParamType, parse: (String => Try[T])) =
      new NonBodyRequestParameter[T](name, description, paramType, location, parse) with Optional[T]
  }
}
