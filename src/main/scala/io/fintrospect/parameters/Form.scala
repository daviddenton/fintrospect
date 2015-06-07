package io.fintrospect.parameters

import com.twitter.io.Charsets._
import org.jboss.netty.handler.codec.http.{HttpRequest, QueryStringDecoder}

import scala.util.Try

/**
 * Builder for parameters that are encoded in the HTTP form.
 */
object Form {
  private val location = new Location {
    override def toString = "form"

    override def from(name: String, request: HttpRequest): Option[String] = {
      Try(new QueryStringDecoder("?" + request.getContent.toString(Utf8)).getParameters.get(name).get(0)).toOption
    }
  }

  val required = new Parameters(RequiredRequestParameter.builder(location))
  val optional = new Parameters(OptionalRequestParameter.builder(location))
}
