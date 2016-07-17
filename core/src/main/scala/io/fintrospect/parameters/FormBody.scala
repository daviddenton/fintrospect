package io.fintrospect.parameters

import com.twitter.finagle.http.Message
import io.fintrospect.ContentTypes.APPLICATION_FORM_URLENCODED
import io.fintrospect.util.{Extraction, ExtractionError, ExtractionFailed, Extractor}
import org.jboss.netty.handler.codec.http.HttpHeaders.Names

import scala.util.{Failure, Success, Try}



class FormBody(val fields: Seq[FormField[_] with Retrieval[Form, _] with Extractor[Form, _]], encodeDecode: FormCodec[Form])
  extends Body[Form](new BodySpec[Form](None, APPLICATION_FORM_URLENCODED, s => encodeDecode.decode(fields, s), f => encodeDecode.encode(f)))
    with Bindable[Form, Binding]
    with Mandatory[Message, Form]
    with MandatoryRebind[Message, Form, Binding] {

  override def iterator = fields.iterator

  def -->(value: Form): Seq[Binding] =
    Seq(new RequestBinding(null, t => {
      val content = spec.serialize(value)
      t.headerMap.add(Names.CONTENT_TYPE, spec.contentType.value)
      t.headerMap.add(Names.CONTENT_LENGTH, content.length.toString)
      t.setContentString(content)
      t
    })) ++ fields.map(f => new FormFieldBinding(f, ""))

  override def <--?(message: Message): Extraction[Form] =
    Try(spec.deserialize(message.contentString)) match {
      case Success(form) => encodeDecode.extract(fields, form)
      case Failure(e) => ExtractionFailed(fields.filter(_.required).map(param => ExtractionError(param, "Could not parse")))
    }
}
