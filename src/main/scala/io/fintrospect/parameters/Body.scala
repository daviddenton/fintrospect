package io.fintrospect.parameters

import argo.jdom.JsonRootNode
import io.fintrospect.ContentType
import io.fintrospect.ContentTypes.APPLICATION_JSON
import io.fintrospect.util.ArgoUtil
import org.jboss.netty.handler.codec.http.HttpRequest

trait Body[T] {
  val contentType: ContentType
  val example: Option[JsonRootNode]
  def from(request: HttpRequest): T
  def parameterParts: Seq[BodyParameter[_]]
}

object Body {

  /**
   * Defines the JSON body of a request.
   * @param description
   * @param example
   */
  def json(description: Option[String], example: JsonRootNode): Body[JsonRootNode] = {
    new UniBody[JsonRootNode](APPLICATION_JSON, description, ObjectParamType, Some(example), ArgoUtil.parse, ArgoUtil.compact)
  }
}
