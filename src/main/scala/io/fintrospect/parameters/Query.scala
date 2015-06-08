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
      Try(parseParams(request.getUri).get(name)).map(_.get(0)).toOption
    }

    private def parseParams(s: String) = {
      Try(new QueryStringDecoder(s).getParameters).toOption.getOrElse(new java.util.HashMap[String, JList[String]])
    }

    override def into(name: String, value: String, request: HttpRequest): Unit = ???
  }

  val required = new Parameters[MandatoryRequestParameter, Mandatory] {
    override protected def parameter[T](name: String, description: Option[String], paramType: ParamType, parse: (String => T)) =
      new MandatoryRequestParameter[T](name, location, description, paramType, parse)
  }

  val optional = new Parameters[OptionalRequestParameter, Optional] {
    override protected def parameter[T](name: String, description: Option[String], paramType: ParamType, parse: (String => T)) =
      new OptionalRequestParameter[T](name, location, description, paramType, parse)
  }
}