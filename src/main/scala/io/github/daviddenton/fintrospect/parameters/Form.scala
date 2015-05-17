package io.github.daviddenton.fintrospect.parameters

import java.util.{List => JList}

import com.twitter.io.Charsets
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
      val params = Try(new QueryStringDecoder("?" + request.getContent.toString(Charsets.Utf8)).getParameters).toOption.getOrElse(new java.util.HashMap[String, JList[String]])
      Option(params.get(name)).map(_.get(0))
    }
  }

  val required = new Parameters(RequiredRequestParameter.builderFor(location))
  val optional = new Parameters(OptionalRequestParameter.builderFor(location))
}
