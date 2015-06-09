package io.fintrospect.parameters

import java.util.{HashMap => JMap, List => JList}

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
      Try(new QueryStringDecoder(s).getParameters).toOption.getOrElse(new JMap[String, JList[String]])
    }

    override def into(name: String, value: String, request: HttpRequest): Unit = ???
  }

  val required = new Parameters[RequestParameter, Mandatory] {
    override protected def parameter[T](name: String, description: Option[String], paramType: ParamType,
                                        deserialize: String => T, serialize: T => String) =
      new RequestParameter[T](name, description, paramType, location, deserialize, serialize) with Mandatory[T]
  }

  val optional = new Parameters[RequestParameter, Optional] {
    override protected def parameter[T](name: String, description: Option[String], paramType: ParamType,
                                        deserialize: String => T, serialize: T => String) =
      new RequestParameter[T](name, description, paramType, location, deserialize, serialize) with Optional[T]
  }}