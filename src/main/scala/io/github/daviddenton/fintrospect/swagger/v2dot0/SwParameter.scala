package io.github.daviddenton.fintrospect.swagger.v2dot0

import argo.jdom.JsonNode
import argo.jdom.JsonNodeFactories._
import io.github.daviddenton.fintrospect.swagger.v2dot0.Location.Location
import io.github.daviddenton.fintrospect.util.ArgoUtil._

case class SwParameter(private val name: String, private val location: Location, private val paramType: String) {
  protected[v2dot0] def toJson: JsonNode = obj(
    "in" -> string(location.toString),
    "name" -> string(name),
    "required" -> booleanNode(true),
    "type" -> string(paramType)
  )
}
