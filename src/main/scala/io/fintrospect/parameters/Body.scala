package io.fintrospect.parameters

import argo.jdom.JsonRootNode
import io.fintrospect.ContentType
import org.jboss.netty.handler.codec.http.HttpRequest

import scala.xml.Elem

abstract class Body[T](spec: BodySpec[T]) extends Iterable[BodyParameter] with Retrieval[T, HttpRequest] {
  val contentType: ContentType = spec.contentType

  def validate(request: HttpRequest): Seq[Either[Parameter, Option[_]]]
}

object Body {

  /**
   * Create a custom body type for the request. Encapsulates the means to insert/retrieve into the request
   */
  def apply[T](bodySpec: BodySpec[T]): UniBody[T] = new UniBody[T](bodySpec, StringParamType, None)

  def json(description: Option[String], example: JsonRootNode = null): UniBody[JsonRootNode] =
    new UniBody[JsonRootNode](BodySpec.json(description), ObjectParamType, Option(example))

  def xml(description: Option[String], example: Elem = null): UniBody[Elem] =
    new UniBody[Elem](BodySpec.xml(description), StringParamType, Option(example))

  def form(fields: FormField[_] with Retrieval[_, Form]*): FormBody = new FormBody(fields)
}
