package io.fintrospect.parameters

import io.fintrospect.{ContentType, ContentTypes}
import org.jboss.netty.handler.codec.http.HttpRequest

case class Form private(override val parameterParts: List[BodyParameter[_]]) extends Body[List[_]] {

  override val example = None

  def withField(field: FormField[_]) = copy(field :: parameterParts)

  override val contentType: ContentType = ContentTypes.APPLICATION_FORM_URLENCODED

  override def from(request: HttpRequest): List[_] = ???
}


/**
 * Builder for parameters that are encoded in the HTTP form.
 */
object Form {
  def apply(fields: FormField[_]*): Form = Form(fields.toList)
}
