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

  def apply[T](bodySpec: BodySpec[T]): Body[T] =
    new UniBody[T](bodySpec, StringParamType, None)

  /**
   * Defines the JSON body of a request.
   * @param description
   * @param example
   */
  def json(description: Option[String], example: JsonRootNode): Body[JsonRootNode] =
    new UniBody[JsonRootNode](BodySpec(description, APPLICATION_JSON, ArgoUtil.parse, ArgoUtil.compact), ObjectParamType, Some(example))

  /**
   * Builder for parameters that are encoded in the HTTP form.
   * @param fields the form fields
   */
  def form(fields: FormField[_]*): Body[List[_]] = new Form(fields.toList)
}
