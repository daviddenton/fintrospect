package io.github.daviddenton.fintrospect.swagger.v1dot1

import argo.jdom.JsonNode
import argo.jdom.JsonNodeFactories._
import io.github.daviddenton.fintrospect.util.ArgoUtil._
import org.jboss.netty.handler.codec.http.HttpMethod

case class SwPathMethod(private val method: HttpMethod, private val summary: String, private val params: Seq[SwParameter], private val responses: Seq[SwResponse]) {
  protected[v1dot1] def toJsonPair: (String, JsonNode) = method.getName.toLowerCase -> obj(
    "summary" -> string(summary),
    "produces" -> array(string("application/json")),
    "parameters" -> array(params.map(_.toJson): _*),
    "responses" -> obj(responses.map(_.toJsonPair))
  )
}
