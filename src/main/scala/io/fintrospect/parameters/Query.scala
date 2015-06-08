package io.fintrospect.parameters

import java.util.{List => JList}

import org.jboss.netty.handler.codec.http.{HttpRequest, QueryStringDecoder}

import scala.util.Try

/**
 * Builder for parameters that are encoded in the HTTP query.
 */
object Query {
  private val location = new Location {
    override def toString = "query"

    override def from(name: String, request: HttpRequest): Option[String] = {
      Option(parseParams(request.getUri).get(name)).map(_.get(0))
    }

    private def parseParams(s: String) = {
      Try(new QueryStringDecoder(s).getParameters).toOption.getOrElse(new java.util.HashMap[String, JList[String]])
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