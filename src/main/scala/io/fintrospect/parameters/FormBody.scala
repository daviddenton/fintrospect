package io.fintrospect.parameters

import com.twitter.io.Charsets
import io.fintrospect.ContentTypes
import io.fintrospect.util.HttpRequestResponseUtil._
import org.jboss.netty.buffer.ChannelBuffers._
import org.jboss.netty.handler.codec.http.HttpHeaders.Names
import org.jboss.netty.handler.codec.http.{HttpRequest, QueryStringDecoder, QueryStringEncoder}

import scala.collection.JavaConverters._
import scala.util.{Failure, Success, Try}


class FormBody(fields: Seq[FormField[_] with Retrieval[_, Form]])
  extends Body[Form](FormBody.spec)
  with Bindable[Form, Binding]
  with MandatoryRebind[Form, HttpRequest, Binding] {

  override def -->(value: Form): Seq[Binding] = {
    Seq(new RequestBinding(null, t => {
      val content = copiedBuffer(FormBody.spec.serialize(value), Charsets.Utf8)
      t.headers().add(Names.CONTENT_TYPE, FormBody.spec.contentType.value)
      t.headers().add(Names.CONTENT_LENGTH, String.valueOf(content.readableBytes()))
      t.setContent(content)
      t
    })) ++ fields.map(f => new FormFieldBinding(f, f.name, ""))
  }

  override def <--(request: HttpRequest) = FormBody.spec.deserialize(contentFrom(request))

  override def iterator = fields.iterator

  override def validate(request: HttpRequest): Seq[Either[Parameter[_], Option[_]]] = {
    Try(contentFrom(request)) match {
      case Success(r) => Try(FormBody.spec.deserialize(r)) match {
        case Success(v) => fields.map(_.validate(v))
        case Failure(_) => fields.filter(!_.required).map(Left(_))
      }
      case _ => fields.filter(!_.required).map(Left(_))
    }
  }
}

object FormBody {
  private val spec = new BodySpec[Form](None,
    ContentTypes.APPLICATION_FORM_URLENCODED,
    content => new Form(new QueryStringDecoder("?" + content).getParameters.asScala.mapValues(_.asScala.toSet)),
    form => {
      val encoder = new QueryStringEncoder("")
      form.foreach(entry => encoder.addParam(entry._1, entry._2.mkString(",")))
      encoder.toUri.getQuery
    }
  )
}