package io.fintrospect.parameters

import io.fintrospect.util.HttpRequestResponseUtil._
import org.jboss.netty.handler.codec.http.{HttpRequest, QueryStringDecoder}

import scala.util.Try

abstract class FormField[T](name: String,
                            description: Option[String],
                            paramType: ParamType,
                            deserialize: String => T,
                            serialize: T => String)
  extends RequestParameter[T](name, description, paramType, FormField.location, deserialize, serialize) {
}

object FormField {
  private val location = new Location {
    override def toString = "form"

    override def from(name: String, request: HttpRequest): Option[String] = {
      Try(new QueryStringDecoder("?" + contentFrom(request)).getParameters.get(name).get(0)).toOption
    }

    override def into(name: String, value: String, request: HttpRequest): Unit = ???
  }

  val required = new Parameters[FormField, Mandatory] {
    override def apply[T](spec: ParameterSpec[T]) =
      new FormField[T](spec.name, spec.description, spec.paramType, spec.deserialize, spec.serialize) with Mandatory[T]
  }

  val optional = new Parameters[FormField, Optional] {
    override def apply[T](spec: ParameterSpec[T]) =
      new FormField[T](spec.name, spec.description, spec.paramType, spec.deserialize, spec.serialize) with Optional[T]
  }
}
