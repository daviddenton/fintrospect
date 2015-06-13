package io.fintrospect.parameters

import io.fintrospect.util.HttpRequestResponseUtil._
import org.jboss.netty.handler.codec.http.{HttpRequest, QueryStringDecoder}

import scala.util.Try

abstract class FormField[T](spec: ParameterSpec[T]) extends BodyParameter[T](spec, FormField.location, Option.empty)

object FormField {
  private val location = new Location {
    override def toString = "form"

    override def from(name: String, request: HttpRequest): Option[String] = {
      Try(new QueryStringDecoder("?" + contentFrom(request)).getParameters.get(name).get(0)).toOption
    }

    override def into(name: String, value: String, request: HttpRequest): Unit = ???
  }

  val required = new Parameters[FormField, Mandatory] {
    override def apply[T](spec: ParameterSpec[T]) = new FormField[T](spec) with Mandatory[T] {}
  }

  val optional = new Parameters[FormField, Optional] {
    override def apply[T](spec: ParameterSpec[T]) = new FormField[T](spec) with Optional[T] {}
  }
}
