package io.fintrospect.parameters

import io.fintrospect.ContentTypes.{APPLICATION_JSON, APPLICATION_XML}
import io.fintrospect.formats.json.{Argo, JsonFormat}
import io.fintrospect.{ContentType, ContentTypes}

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
case class BodySpec[T](description: Option[String], contentType: ContentType, deserialize: String => T, serialize: T => String = (s: T) => s.toString) {
  /**
    * Combined map and reverse-map operation
    */
  def map[O](in: T => O, out: O => T): BodySpec[O] = BodySpec[O](description, contentType, s => in(deserialize(s)), b => serialize(out(b)))

  /**
    * Traditional map operation. Duh.
    */
  def map[O](in: T => O) = BodySpec[O](description, contentType, s => in(deserialize(s)))
}

object BodySpec {
  def string(description: Option[String] = None, contentType: ContentType = ContentTypes.TEXT_PLAIN): BodySpec[String] = BodySpec[String](description, contentType, identity, identity)

  def json[T](description: Option[String] = None, jsonFormat: JsonFormat[T, _] = Argo.JsonFormat): BodySpec[T] = BodySpec[T](description, APPLICATION_JSON, jsonFormat.parse, jsonFormat.compact)

  def xml(description: Option[String] = None): BodySpec[Elem] = BodySpec[Elem](description, APPLICATION_XML, XML.loadString, _.toString())
}