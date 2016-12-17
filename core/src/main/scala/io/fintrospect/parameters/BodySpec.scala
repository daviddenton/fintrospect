package io.fintrospect.parameters

import com.twitter.io.Buf
import com.twitter.io.Buf.ByteArray.Shared.extract
import io.fintrospect.ContentType
import io.fintrospect.ContentTypes.{APPLICATION_JSON, APPLICATION_XML, TEXT_PLAIN}
import io.fintrospect.formats.{Argo, JsonLibrary}

import scala.xml.{Elem, XML}

/**
  * Spec required to marshall a body of a custom type
  *
  * @param description Description to be used in the documentation
  * @param contentType The HTTP content type header value
  * @param paramType How the body is represented when documenting APIs.
  * @param deserialize function to take the input string from the request and attempt to construct a deserialized instance. Exceptions are
  *                    automatically caught and translated into the appropriate result, so just concentrate on the Happy-path case
  * @param serialize   function to take the input type and serialize it to a string to be represented in the request
  * @tparam T the type of the parameter
  */
case class BodySpec[T](description: Option[String], contentType: ContentType, paramType: ParamType, deserialize: Buf => T, serialize: T => Buf = (s: T) => Buf.Utf8(s.toString)) {

  /**
    * Bi-directional map functions for this ParameterSpec type. Use this to implement custom Parameter types
    */
  def map[O](in: T => O, out: O => T): BodySpec[O] = BodySpec[O](description, contentType, paramType, s => in(deserialize(s)), b => serialize(out(b)))

  /**
    * Uni-directional map functions for this BodySpec type. Use this to implement custom Body types
    */
  def map[O](in: T => O) = BodySpec[O](description, contentType, paramType, s => in(deserialize(s)))
}

object BodySpec {
  def string(description: Option[String] = None, contentType: ContentType = TEXT_PLAIN): BodySpec[String] =
    BodySpec[String](description, contentType, StringParamType, b => new String(extract(b)))

  def json[T](description: Option[String] = None, jsonLib: JsonLibrary[T, _] = Argo): BodySpec[T] =
    BodySpec[T](description, APPLICATION_JSON, ObjectParamType, b => jsonLib.JsonFormat.parse(new String(extract(b))), t => Buf.Utf8(jsonLib.JsonFormat.compact(t)))

  def xml(description: Option[String] = None): BodySpec[Elem] = string(description, APPLICATION_XML).map(XML.loadString, _.toString())

  def binary(description: Option[String] = None, contentType: ContentType): BodySpec[Buf] = BodySpec(description, contentType, FileParamType,
    b => {
      require(b.length > 0)
      b
    },
    b => {
      require(b.length > 0)
      b
    }
  )
}