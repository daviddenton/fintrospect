package io.fintrospect.parameters

import argo.jdom.JsonRootNode
import io.fintrospect.ContentType
import io.fintrospect.ContentTypes._
import io.fintrospect.util.HttpRequestResponseUtil._
import org.jboss.netty.handler.codec.http.{HttpRequest, QueryStringDecoder}

import scala.collection.JavaConverters._

trait BodyParameter[T] extends Parameter[T] {
  val example: Option[JsonRootNode]
}

class FormBody(fields: Seq[FormField[_] with Retrieval[_, Form]]) extends Body[Form] {
  override val contentType: ContentType = APPLICATION_FORM_URLENCODED

  override def from(request: HttpRequest): Form = {
    new Form(new QueryStringDecoder("?" + contentFrom(request)).getParameters.asScala.mapValues(_.asScala.toSet))
  }

  override def iterator = fields.iterator

  override def validate(request: HttpRequest) = {
    val form = from(request)
    fields.map(_.validate(form))
  }
}
