package io.fintrospect.parameters

import io.fintrospect.{ContentType, ContentTypes}

case class Form private(private val fields: List[Parameter[_]]) extends Body {
  override def iterator: Iterator[Parameter[_]] = fields.iterator

  def withField(field: Parameter[_]) = copy(field :: fields)

  override val contentType: ContentType = ContentTypes.APPLICATION_FORM_URLENCODED
}


/**
 * Builder for parameters that are encoded in the HTTP form.
 */
object Form {
  def apply(fields: Parameter[_]*): Form = Form(fields.toList)
}
