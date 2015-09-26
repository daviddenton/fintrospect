package io.fintrospect.parameters

import argo.jdom.JsonRootNode
import io.fintrospect.ContentType
import io.fintrospect.ContentTypes._
import io.fintrospect.util.json.ArgoJsonFormat._

import scala.xml.{Elem, XML}

/**
 * Spec required to marshall a body of a custom type
 * @param description Description to be used in the documentation
 * @param contentType The HTTP content type header value
 * @param deserialize function to take the input string from the request and attempt to construct a deserialized instance. Exceptions are
 *                    automatically caught and translated into the appropriate result, so just concentrate on the Happy-path case
 * @param serialize function to take the input type and serialize it to a string to be represented in the request
 * @tparam T the type of the parameter
 */
case class BodySpec[T](description: Option[String], contentType: ContentType, deserialize: String => T, serialize: T => String)

object BodySpec {
  def json(description: Option[String] = None): BodySpec[JsonRootNode] = BodySpec[JsonRootNode](description, APPLICATION_JSON, parse, compact)
  def xml(description: Option[String] = None): BodySpec[Elem] = BodySpec[Elem](description, APPLICATION_XML, XML.loadString, _.toString())
}