package io.fintrospect.parameters

import argo.jdom.JsonNode
import io.fintrospect.util.ArgoUtil
import io.fintrospect.util.HttpRequestResponseUtil.contentFrom
import org.jboss.netty.handler.codec.http.HttpRequest

import scala.util.Try

class Body private(val description: Option[String], val paramType: ParamType, val location: Location, val example: JsonNode, parse: (String => JsonNode))
  extends RequestParameter[JsonNode, JsonNode]("body", location, parse, true) {

  override def from(request: HttpRequest): JsonNode = parse(location.from(null, request).get)

  override def parseFrom(request: HttpRequest): Option[Try[JsonNode]] = Some(Try(from(request)))
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
