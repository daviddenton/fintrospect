package io.github.daviddenton.fintrospect.parameters

import argo.jdom.JsonNode
import com.twitter.io.Charsets
import io.github.daviddenton.fintrospect.FinagleTypeAliases
import io.github.daviddenton.fintrospect.util.ArgoUtil

import scala.util.Try

class Body private(description: Option[String], paramType: ParamType, val example: JsonNode, parse: (String => Option[JsonNode]))
  extends Parameter[JsonNode]("body", description, "body", paramType) {
  val requirement = Requirement.Mandatory

  def from(request: FinagleTypeAliases.FTRequest): JsonNode = parse(request.getContent.toString(Charsets.Utf8)).get
}

object Body {

  /**
   * Defines the JSON body of a request.
   * @param description
   * @param example
   */
  def json(description: Option[String], example: JsonNode) = new Body(description, ObjectParamType, example, s => Try(ArgoUtil.parse(s)).toOption)
}
