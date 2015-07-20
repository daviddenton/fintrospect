package io.fintrospect.parameters

import argo.jdom.JsonRootNode
import io.fintrospect.ContentType
import io.fintrospect.ContentTypes._
import io.fintrospect.util.ArgoUtil
import org.jboss.netty.handler.codec.http.HttpRequest

abstract class Body[T](spec: BodySpec[T]) extends Iterable[BodyParameter] with Retrieval[T, HttpRequest] {
  val contentType: ContentType = spec.contentType

  def validate(request: HttpRequest): Seq[Either[Parameter, Option[_]]]
}

object Body {

  /**
   * Create a custom body type for the request. Encapsulates the means to insert/retrieve into the request
   */
  def apply[T](bodySpec: BodySpec[T]): Body[T] = new UniBody[T](bodySpec, StringParamType, None)

  def json(description: Option[String], example: JsonRootNode = null): UniBody[JsonRootNode] =
    new UniBody[JsonRootNode](BodySpec(description, APPLICATION_JSON, ArgoUtil.parse, ArgoUtil.compact), ObjectParamType, Option(example))

  def form(fields: FormField[_] with Retrieval[_, Form]*): FormBody = new FormBody(fields)
}
