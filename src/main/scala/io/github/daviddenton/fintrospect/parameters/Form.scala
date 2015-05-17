package io.github.daviddenton.fintrospect.parameters

import com.twitter.io.Charsets._
import io.github.daviddenton.fintrospect.FinagleTypeAliases.FTRequest
import org.jboss.netty.handler.codec.http.QueryStringDecoder

import scala.util.Try

/**
 * Builder for parameters that are encoded in the HTTP form.
 */
object Form {
  private val location = new Location {
    override def toString = "form"

    override def from(name: String, request: FTRequest): Option[String] = {
      Try(new QueryStringDecoder("?" + request.getContent.toString(Utf8)).getParameters.get(name).get(0)).toOption
    }
  }

  val required = new Parameters(RequiredRequestParameter.builderFor(location))
  val optional = new Parameters(OptionalRequestParameter.builderFor(location))
}
