package io.fintrospect.parameters

import argo.jdom.JsonNode
import io.fintrospect.util.ArgoUtil
import io.fintrospect.util.HttpRequestResponseUtil.contentFrom
import org.jboss.netty.handler.codec.http.HttpRequest

import scala.util.Try

class Body private(description: Option[String], paramType: ParamType, location: Location, val example: JsonNode, parse: (String => JsonNode))
  extends MandatoryRequestParameter[JsonNode]("body", location, description, paramType, parse) {
}

object Body {
  private val location = new Location {
    override def toString = "body"

    override def from(unused: String, request: HttpRequest): Option[String] = Try(contentFrom(request)).toOption
  }

  /**
   * Defines the JSON body of a request.
   * @param description
   * @param example
   */
  def json(description: Option[String], example: JsonNode) =
    new Body(description, ObjectParamType, location, example, s => ArgoUtil.parse(s))
}
