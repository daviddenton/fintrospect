package io.fintrospect.parameters

import com.twitter.finagle.http._
import io.fintrospect.ContentTypes.{APPLICATION_FORM_URLENCODED, MULTIPART_FORM}
import io.fintrospect.util.{Extraction, ExtractionError, ExtractionFailed, Extractor}
import org.jboss.netty.handler.codec.http.HttpHeaders.Names

import scala.util.{Failure, Success, Try}

class UrlEncodedFormBody(val fields: Seq[FormField[_] with Extractor[Form, _]], encodeDecode: FormCodec)
  extends Body[Form] {

  override val contentType = APPLICATION_FORM_URLENCODED

  override def iterator = fields.iterator

  def -->(value: Form): Seq[RequestBinding] =
    Seq(new RequestBinding(null, req => {
      val contentString = encodeDecode.encode(value)
      req.headerMap.add(Names.CONTENT_TYPE, contentType.value)
      req.headerMap.add(Names.CONTENT_LENGTH, contentString.length.toString)
      req.contentString = contentString
      req
    })) ++ fields.map(f => new FormFieldBinding(f, ""))

  override def <--?(message: Message): Extraction[Form] =
    Try(encodeDecode.decode(fields, message)) match {
      case Success(form) => encodeDecode.extract(fields, form)
      case Failure(e) => ExtractionFailed(fields.filter(_.required).map(param => ExtractionError(param, "Could not parse")))
    }
}

class MultiPartFormBody(val fields: Seq[FormField[_] with Extractor[Form, _]], encodeDecode: FormCodec)
  extends Body[Form] {

  override val contentType = MULTIPART_FORM

  override def iterator = fields.iterator

  def -->(value: Form): Seq[RequestBinding] =
    Seq(new RequestBinding(null, req => {
      val fields = value.fields.flatMap(f => f._2.map(g => SimpleElement(f._1, g))).toSeq
      val files = value.files.flatMap(f => f._2.map(g => FileElement(f._1, null, null, null))).toSeq
      val headers = Map(req.headerMap.toSeq: _*)
      RequestBuilder().url(req.uri).addHeaders(headers).add(fields).add(files).buildFormPost(multipart = true)
    }))

  override def <--?(message: Message): Extraction[Form] = message.asInstanceOf[Request].multipart
    .map(m => {
      val multipart = message.asInstanceOf[Request].multipart.get
      Try(Form(
        multipart.attributes.mapValues(_.toSet),
        multipart.files.mapValues(_.toSet)
      )) match {
        case Success(form) => encodeDecode.extract(fields, form)
        case Failure(e) => ExtractionFailed(fields.filter(_.required).map(param => ExtractionError(param, "Could not parse")))
      }
    }).getOrElse(ExtractionFailed(fields.filter(_.required).map(param => ExtractionError(param, "Could not parse"))))
}
