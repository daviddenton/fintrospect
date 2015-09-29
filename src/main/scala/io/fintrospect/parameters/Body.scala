package io.fintrospect.parameters

import io.fintrospect.ContentType
import io.fintrospect.formats.json.{Argo, JsonFormat}
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
  def apply[T](bodySpec: BodySpec[T], example: T = null, paramType: ParamType = StringParamType): UniBody[T] = new UniBody[T](bodySpec, paramType, Option(example))

  def json[T](description: Option[String], example: T = null, jsonFormat: JsonFormat[T, _] = Argo.JsonFormat): UniBody[T] = Body(BodySpec.json(description, jsonFormat), example, ObjectParamType)

  def xml(description: Option[String], example: Elem = null): UniBody[Elem] = Body(BodySpec.xml(description), example, StringParamType)

  def form(fields: FormField[_] with Retrieval[_, Form]*): FormBody = new FormBody(fields)
}
