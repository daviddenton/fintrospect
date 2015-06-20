package io.fintrospect.parameters

import java.util.{HashMap => JMap, List => JList}

import org.jboss.netty.handler.codec.http.{HttpRequest, QueryStringDecoder}

import scala.util.Try

object Query {
  private val location = new Location {
    override def toString = "query"

    override def from(name: String, request: HttpRequest): Option[String] = {
      Try(new QueryStringDecoder(request.getUri).getParameters.get(name)).map(_.get(0)).toOption
    }

    override def into(name: String, value: String, request: HttpRequest): Unit = ???
  }

  val required = new Parameters[RequestParameter, Mandatory] {
    override def apply[T](spec: ParameterSpec[T]) = new RequestParameter[T](spec, location) with Mandatory[T]
  }

  val optional = new Parameters[RequestParameter, Optional] {
    override def apply[T](spec: ParameterSpec[T]) = new RequestParameter[T](spec, location) with Optional[T]
  }
}