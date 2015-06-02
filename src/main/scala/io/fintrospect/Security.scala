package io.fintrospect

import argo.jdom.JsonNode
import io.fintrospect.util.ArgoUtil._

/**
 * Represents the security model of a Route.
 */
case class Security private(name: String, definition: JsonNode) {
  def toPathSecurity: (String, JsonNode) = name -> definition
}

object Security {
  def apiKey(name: String): Security = {
    Security(name, obj(
      "type" -> string("api_key"),
      "name" -> string(name),
      "in" -> string("header")))
  }
}
