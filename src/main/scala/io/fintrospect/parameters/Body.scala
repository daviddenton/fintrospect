package io.fintrospect.parameters

import argo.jdom.JsonRootNode
import io.fintrospect.ContentType
import io.fintrospect.ContentTypes._
import io.fintrospect.util.ArgoUtil
import org.jboss.netty.handler.codec.http.HttpRequest

trait Body[T] extends Iterable[BodyParameter[_]] with Retrieval[T, HttpRequest] {
  val contentType: ContentType

  def validate(request: HttpRequest): Seq[Either[Parameter[_], Option[_]]]
}

object Body {

  /**
   * Create a custom body type for the request. Encapsulates the means to insert/retrieve into the request
   */
  def apply[T](bodySpec: BodySpec[T]): Body[T] = new UniBody[T](bodySpec, StringParamType, None)

  def json(description: Option[String], example: JsonRootNode): Body[JsonRootNode] =
    new UniBody[JsonRootNode](BodySpec(description, APPLICATION_JSON, ArgoUtil.parse, ArgoUtil.compact), ObjectParamType, Some(example))

  def form(fields: FormField[_] with Retrieval[_, Form]*): Body[Form] = new FormBody(fields)
}
