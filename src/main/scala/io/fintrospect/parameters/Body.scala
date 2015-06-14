package io.fintrospect.parameters

import argo.jdom.JsonRootNode
import io.fintrospect.ContentType
import io.fintrospect.ContentTypes._
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
   * Create a custom body type for the request. Encapsulates the means to insert/retrieve into the request
   */
  def apply[T](bodySpec: BodySpec[T]): Body[T] = new UniBody[T](bodySpec, StringParamType, None)

  def json(description: Option[String], example: JsonRootNode): Body[JsonRootNode] =
    new UniBody[JsonRootNode](BodySpec(description, APPLICATION_JSON, ArgoUtil.parse, ArgoUtil.compact), ObjectParamType, Some(example))

  def form(fields: FormField[_] with Retrieval[_]*): Body[Form] = new Body[Form] {
    override val example = None
    override val contentType = APPLICATION_FORM_URLENCODED
    override def from(request: HttpRequest) = new Form(request)
    override def parameterParts = fields
  }
}
