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
  * @param contentType The HTTP content type header value
  * @param paramType How the body is represented when documenting APIs.
  * @param deserialize function to take the input string from the request and attempt to construct a deserialized instance. Exceptions are
  *                    automatically caught and translated into the appropriate result, so just concentrate on the Happy-path case
  * @param serialize   function to take the input type and serialize it to a string to be represented in the request
  * @tparam T the type of the deserialised body
  */
case class BodySpec[T](contentType: ContentType, paramType: ParamType, deserialize: Buf => T, serialize: T => Buf = (s: T) => Buf.Utf8(s.toString)) {

  /**
    * Bi-directional map functions for this ParameterSpec type. Use this to implement custom Parameter types
    */
  def map[O](in: T => O, out: O => T): BodySpec[O] = BodySpec[O](contentType, paramType, s => in(deserialize(s)), b => serialize(out(b)))

  /**
    * Uni-directional map functions for this BodySpec type. Use this to implement custom Body types
    */
  def map[O](in: T => O) = BodySpec[O](contentType, paramType, s => in(deserialize(s)))
}

object BodySpec {
  def string(contentType: ContentType = TEXT_PLAIN, validation: StringValidations.Rule = StringValidations.EmptyIsInvalid): BodySpec[String] =
    BodySpec[String](contentType, StringParamType, b => validation(new String(extract(b))))

  def json[T](jsonLib: JsonLibrary[T, _] = Argo): BodySpec[T] =
    BodySpec[T](APPLICATION_JSON, ObjectParamType, b => jsonLib.JsonFormat.parse(new String(extract(b))), t => Buf.Utf8(jsonLib.JsonFormat.compact(t)))

  def xml(): BodySpec[Elem] = string(APPLICATION_XML).map(XML.loadString, _.toString())

  def binary(contentType: ContentType): BodySpec[Buf] = BodySpec(contentType, FileParamType,
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