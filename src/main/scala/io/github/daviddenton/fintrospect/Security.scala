package io.github.daviddenton.fintrospect

import argo.jdom.JsonNode
import io.github.daviddenton.fintrospect.util.ArgoUtil._

case class Security(private val name: String, private val definition: JsonNode) {
  def toPathSecurity: (String, JsonNode) = name -> obj()

  def toJsonDefinition: (String, JsonNode) = name -> definition
}

object Security {
  def apiKey(name: String): Security = {
    Security(name, obj(
      "type" -> string("api_key"),
      "name" -> string(name),
      "in" -> string("header")))
  }
}