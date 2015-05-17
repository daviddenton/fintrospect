package io.github.daviddenton.fintrospect.parameters

import java.util.{List => JList}

import io.github.daviddenton.fintrospect.FinagleTypeAliases.FTRequest
import org.jboss.netty.handler.codec.http.QueryStringDecoder

import scala.util.Try

/**
 * Builder for parameters that are encoded in the HTTP query.
 */
object Query {
  private val location = new Location {
    override def toString = "query"

    override def from(name: String, request: FTRequest): Option[String] = {
      Option(parseParams(request.getUri).get(name)).map(_.get(0))
    }

    private def parseParams(s: String) = {
      Try(new QueryStringDecoder(s).getParameters).toOption.getOrElse(new java.util.HashMap[String, JList[String]])
    }
  }

  val required = new Parameters(RequiredRequestParameter.builderFor(location))
  val optional = new Parameters(OptionalRequestParameter.builderFor(location))
}