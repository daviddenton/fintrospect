package io.fintrospect.parameters

import com.twitter.finagle.http.Message
import io.fintrospect.ContentTypes.APPLICATION_FORM_URLENCODED
import io.fintrospect.util.{Extraction, ExtractionError, ExtractionFailed, Extractor}
import org.jboss.netty.handler.codec.http.HttpHeaders.Names

import scala.util.{Failure, Success, Try}

class FormBody(val fields: Seq[FormField[_] with Extractor[Form, _]], encodeDecode: FormCodec)
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

  override def <--?(message: Message): Extraction[Form] = {
    Try(encodeDecode.decode(fields, message)) match {
      case Success(form) => encodeDecode.extract(fields, form)
      case Failure(e) => ExtractionFailed(fields.filter(_.required).map(param => ExtractionError(param, "Could not parse")))
    }
  }
}
