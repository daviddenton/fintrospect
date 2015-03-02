package io.github.daviddenton.fintrospect.swagger.v2dot0

import argo.jdom.JsonNode
import argo.jdom.JsonNodeFactories._
import io.github.daviddenton.fintrospect.util.ArgoUtil._

case class SwSecurity(private val name: String, private val definition: JsonNode) {
  protected[v2dot0] def toPathSecurity: (String, JsonNode) = name -> obj()

  protected[v2dot0] def toJsonDefinition: (String, JsonNode) = name -> definition
}

object SwSecurity {
  def apiKey(name: String): SwSecurity = {
    SwSecurity(name, obj(
      "type" -> string("api_key"),
      "name" -> string(name),
      "in" -> string("header")))
  }
}