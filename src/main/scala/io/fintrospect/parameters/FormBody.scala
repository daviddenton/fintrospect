package io.fintrospect.parameters

import com.twitter.io.Charsets
import io.fintrospect.util.HttpRequestResponseUtil._
import org.jboss.netty.buffer.ChannelBuffers._
import org.jboss.netty.handler.codec.http.HttpHeaders.Names
import org.jboss.netty.handler.codec.http.HttpRequest

import scala.util.{Failure, Success, Try}

class FormBody(fields: Seq[FormField[_] with Retrieval[_, Form]]) extends Body[Form](BodySpec.form()) {

  override def ->(value: Form): Seq[Binding] = {
    Seq(RequestBinding(null, t => {
      val content = copiedBuffer(BodySpec.form().serialize(value), Charsets.Utf8)
      t.headers().add(Names.CONTENT_TYPE, BodySpec.form().contentType.value)
      t.headers().add(Names.CONTENT_LENGTH, String.valueOf(content.readableBytes()))
      t.setContent(content)
      t
    })) ++ fields.map(f => FormFieldBinding(f, f.name, ""))
  }

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
