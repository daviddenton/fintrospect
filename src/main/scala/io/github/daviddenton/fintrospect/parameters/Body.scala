package io.github.daviddenton.fintrospect.parameters

import argo.jdom.JsonNode
import com.twitter.finagle.http.Request
import io.github.daviddenton.fintrospect.util.ArgoUtil

import scala.util.Try

class Body private(description: Option[String], paramType: ParamType, val example: JsonNode, parse: (String => Option[JsonNode]))
  extends Parameter[JsonNode]("body", description, "body", paramType) {
  val requirement = Requirement.Mandatory

  def from(request: Request): JsonNode = parse(request.contentString).get
}

object Body {
  def json(description: Option[String], example: JsonNode) = new Body(description, ObjectParamType, example, s => Try(ArgoUtil.parse(s)).toOption)
}
