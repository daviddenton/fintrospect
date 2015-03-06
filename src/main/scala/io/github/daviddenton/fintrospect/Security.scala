package io.github.daviddenton.fintrospect

import argo.jdom.JsonNode
import argo.jdom.JsonNodeFactories._
import io.github.daviddenton.fintrospect.util.ArgoUtil._

case class SwSecurity(private val name: String, private val definition: JsonNode) {
  def toPathSecurity: (String, JsonNode) = name -> obj()

 def toJsonDefinition: (String, JsonNode) = name -> definition
}

object SwSecurity {
  def apiKey(name: String): SwSecurity = {
    SwSecurity(name, obj(
      "type" -> string("api_key"),
      "name" -> string(name),
      "in" -> string("header")))
  }
}