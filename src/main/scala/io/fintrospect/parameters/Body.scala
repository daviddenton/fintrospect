package io.fintrospect.parameters

import argo.jdom.JsonRootNode
import io.fintrospect.util.ArgoUtil
import io.fintrospect.util.HttpRequestResponseUtil.contentFrom
import org.jboss.netty.handler.codec.http.HttpRequest

import scala.util.Try

object Body {
  private val location = new Location {
    override def toString = "body"

    override def from(unused: String, request: HttpRequest): Option[String] = Try(contentFrom(request)).toOption

    override def into(name: String, value: String, request: HttpRequest): Unit = ???
  }

  /**
   * Defines the JSON body of a request.
   * @param description
   * @param example
   */
  def json(description: Option[String], example: JsonRootNode) =
    new BodyParameter[JsonRootNode]("body", description, ObjectParamType, example, location, ArgoUtil.parse, ArgoUtil.compact) with Mandatory[JsonRootNode]
}
