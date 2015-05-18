package io.github.daviddenton.fintrospect.parameters

import argo.jdom.JsonNode
import com.twitter.io.Charsets
import io.github.daviddenton.fintrospect.util.ArgoUtil
import org.jboss.netty.handler.codec.http.HttpRequest

import scala.util.Try

class Body private(val description: Option[String], val paramType: ParamType, val example: JsonNode, val parse: (String => Option[JsonNode])) extends Parameter[JsonNode] {
  override val name = "body"
  override val where = "body"
  override val requirement = Requirement.Mandatory

  def from(request: HttpRequest): JsonNode = parse(request.getContent.toString(Charsets.Utf8)).get
}

object Body {

  /**
   * Defines the JSON body of a request.
   * @param description
   * @param example
   */
  def json(description: Option[String], example: JsonNode) = new Body(description, ObjectParamType, example, s => Try(ArgoUtil.parse(s)).toOption)
}
