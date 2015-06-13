package io.fintrospect.parameters

import io.fintrospect.{ContentType, ContentTypes}
import org.jboss.netty.handler.codec.http.HttpRequest

class Form (override val parameterParts: List[BodyParameter[_]]) extends Body[List[_]] {

  override val example = None

  def withField(field: FormField[_]) = new Form(field :: parameterParts)

  override val contentType: ContentType = ContentTypes.APPLICATION_FORM_URLENCODED

  override def from(request: HttpRequest): List[_] = ???
}
