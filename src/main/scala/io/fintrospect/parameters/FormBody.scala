package io.fintrospect.parameters

import io.fintrospect.ContentType
import io.fintrospect.ContentTypes._
import io.fintrospect.util.HttpRequestResponseUtil._
import org.jboss.netty.handler.codec.http.HttpRequest

import scala.util.{Failure, Success, Try}

class FormBody(fields: Seq[FormField[_] with Retrieval[_, Form]]) extends Body[Form](BodySpec.form()) {
  override val contentType: ContentType = APPLICATION_FORM_URLENCODED

  override def ->(value: Form) = ???

  override def from(request: HttpRequest) = BodySpec.form().deserialize(contentFrom(request))

  override def iterator = fields.iterator

  override def validate(request: HttpRequest): Seq[Either[Parameter[_], Option[_]]] = {
    val from = Try(contentFrom(request)).toOption
    if (from.isEmpty) {
      fields.filter(!_.required).map(Left(_))
    } else {
      Try(BodySpec.form().deserialize(from.get)) match {
        case Success(v) => fields.map(_.validate(v))
        case Failure(_) => fields.filter(!_.required).map(Left(_))
      }
    }
  }
}
