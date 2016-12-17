package io.fintrospect.parameters

import com.twitter.io.Buf
import com.twitter.io.Buf.ByteArray.Shared.extract
import io.fintrospect.ContentType
import io.fintrospect.ContentTypes.{APPLICATION_JSON, APPLICATION_XML, TEXT_PLAIN}
import io.fintrospect.formats.{Argo, JsonFormat}

import scala.xml.{Elem, XML}

/**
  * Spec required to marshall a body of a custom type
  *
  * @param description Description to be used in the documentation
  * @param contentType The HTTP content type header value
  * @param deserialize function to take the input string from the request and attempt to construct a deserialized instance. Exceptions are
  *                    automatically caught and translated into the appropriate result, so just concentrate on the Happy-path case
  * @param serialize   function to take the input type and serialize it to a string to be represented in the request
  * @tparam T the type of the parameter
  */
case class BodySpec[T](description: Option[String], contentType: ContentType, paramType: ParamType, deserialize: Buf => T, serialize: T => Buf = (s: T) => Buf.Utf8(s.toString)) {
  /**
    * Combined map and reverse-map operation
    */
  def map[O](in: T => O, out: O => T): BodySpec[O] = BodySpec[O](description, contentType, paramType, s => in(deserialize(s)), b => serialize(out(b)))

  /**
    * Traditional map operation. Duh.
    */
  def map[O](in: T => O) = BodySpec[O](description, contentType, paramType, s => in(deserialize(s)))
}

object BodySpec {
  def string(description: Option[String] = None, contentType: ContentType = TEXT_PLAIN): BodySpec[String] =
    BodySpec[String](description, contentType, StringParamType, b => new String(extract(b)))

  def json[T](description: Option[String] = None, jsonFormat: JsonFormat[T, _] = Argo.JsonFormat): BodySpec[T] =
    BodySpec[T](description, APPLICATION_JSON, ObjectParamType, b => jsonFormat.parse(new String(extract(b))), t => Buf.Utf8(jsonFormat.compact(t)))

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