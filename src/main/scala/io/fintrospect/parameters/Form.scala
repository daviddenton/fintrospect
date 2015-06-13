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

    override def into(name: String, value: String, request: HttpRequest): Unit = ???
  }

  val required = new Parameters[RequestParameter, Mandatory] {
    override def apply[T](spec: ParameterSpec[T]) =
      new RequestParameter[T](spec.name, spec.description, spec.paramType, location, spec.deserialize, spec.serialize) with Mandatory[T]
  }

  val optional = new Parameters[RequestParameter, Optional] {
    override def apply[T](spec: ParameterSpec[T]) =
      new RequestParameter[T](spec.name, spec.description, spec.paramType, location, spec.deserialize, spec.serialize) with Optional[T]
  }
}
