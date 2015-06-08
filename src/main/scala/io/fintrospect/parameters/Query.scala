package io.fintrospect.parameters

import java.util.{List => JList}

import org.jboss.netty.handler.codec.http.{HttpRequest, QueryStringDecoder}

import scala.util.Try

/**
 * Builder for parameters that are encoded in the HTTP query.
 */
object Query {
  private val aLocation = new Location {
    override def toString = "query"

    override def from(name: String, request: HttpRequest): Option[String] = {
      Option(parseParams(request.getUri).get(name)).map(_.get(0))
    }

    private def parseParams(s: String) = {
      Try(new QueryStringDecoder(s).getParameters).toOption.getOrElse(new java.util.HashMap[String, JList[String]])
    }
  }

  val required = new Parameters[MandatoryRequestParameter] {
    override protected def parameter[T](name: String, description: Option[String], paramType: ParamType, parse: (String => Try[T])) =
      new MandatoryRequestParameter[T](name, aLocation, description, paramType, parse)
  }

  val optional = new Parameters[OptionalRequestParameter] {
    override protected def parameter[T](name: String, description: Option[String], paramType: ParamType, parse: (String => Try[T])) =
      new OptionalRequestParameter[T](name, aLocation, description, paramType, parse)
  }
}