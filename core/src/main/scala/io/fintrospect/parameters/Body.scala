package io.fintrospect.parameters

import com.twitter.finagle.http.Message
import io.fintrospect.ContentType
import io.fintrospect.formats.{Argo, JsonFormat}
import io.fintrospect.util.Extractor

import scala.xml.Elem

abstract class Body[T](protected val spec: BodySpec[T]) extends Iterable[BodyParameter]
  with Mandatory[Message, T]
  with Rebindable[Message, T, RequestBinding] {

  override def <->(from: Message): Iterable[RequestBinding] = this --> (this <-- from)

  val contentType: ContentType = spec.contentType
}

/**
  * Factory methods for various supported HTTP body types.
  */
object Body {

  /**
    * Create a custom body type for the request. Encapsulates the means to insert/retrieve into the request
    */
  def apply[T](bodySpec: BodySpec[T], example: T = null, paramType: ParamType = StringParamType): UniBody[T] = new UniBody[T](bodySpec, paramType, Option(example))

  /**
    * JSON format HTTP message body. Defaults to Argo JSON format, but this can be overridden by passing an alternative JsonFormat
    */
  def json[T](description: Option[String], example: T = null, jsonFormat: JsonFormat[T, _] = Argo.JsonFormat): UniBody[T] = Body(BodySpec.json(description, jsonFormat), example, ObjectParamType)

  /**
    * Native Scala XML format HTTP message body.
    */
  def xml(description: Option[String], example: Elem = null): UniBody[Elem] = Body(BodySpec.xml(description), example, StringParamType)

  /**
    * HTML encoded form HTTP message body which will fail to deserialize if a single field is missing/invalid. Use this
    * for server-server communications when you want the server to reject with a BadRequest.
    * This method simply takes a set of form fields.
    */
  def form(fields: FormField[_] with Extractor[Form, _]*): FormBody = new FormBody(fields, new StrictFormCodec())

  /**
    * HTML encoded form HTTP message body which deserializes even if fields are missing/invalid. Use this
    * for browser-server communications where you want to give feedback to the user.
    * This method takes a set of form fields, combined with their relevant error messages in case of validation failure.
    */
  def webForm(fields: (FormField[_] with Extractor[Form, _], String)*): FormBody = new FormBody(fields.map(_._1), new WebFormCodec(Map(fields: _*)))
}
