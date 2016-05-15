package io.fintrospect.parameters

import java.net.URLDecoder.decode
import java.net.URLEncoder.encode

import com.twitter.finagle.http.Message
import io.fintrospect.ContentTypes
import io.fintrospect.util.HttpRequestResponseUtil.contentFrom
import org.jboss.netty.handler.codec.http.HttpHeaders.Names

import scala.util.{Failure, Success, Try}


/**
  * Forms are effectively modelled as a collection of fields.
  */
class FormBody(fields: Seq[FormField[_] with Retrieval[_, Form] with Validatable[_, Form]])
  extends Body[Form](FormBody.spec)
  with Bindable[Form, Binding]
  with MandatoryRebind[Form, Message, Binding] {

  override def -->(value: Form): Seq[Binding] =
    Seq(new RequestBinding(null, t => {
      val content = FormBody.spec.serialize(value)
      t.headerMap.add(Names.CONTENT_TYPE, FormBody.spec.contentType.value)
      t.headerMap.add(Names.CONTENT_LENGTH, content.length.toString)
      t.setContentString(content)
      t
    })) ++ fields.map(f => new FormFieldBinding(f, ""))

  override def iterator = fields.iterator

  override def <--?(message: Message): Extraction[Form] =
    Try(FormBody.spec.deserialize(contentFrom(message))) match {
      case Success(form) => {
        val missingOrInvalidFields = fields.map(_.validate(form)).flatMap(_.invalid)
        if (missingOrInvalidFields.isEmpty) Extracted(form) else ExtractionFailed(missingOrInvalidFields)
      }
      case Failure(e) => ExtractionFailed(fields.filter(_.required).map(InvalidParameter(_, "Could not parse")))
    }
}

object FormBody {
  private val spec = new BodySpec[Form](None, ContentTypes.APPLICATION_FORM_URLENCODED, decodeForm, encodeForm)

  private def encodeForm(form: Form): String = form.flatMap {
    case (name, values) => values.map {
      case value => encode(name, "UTF-8") + "=" + encode(value, "UTF-8")
    }
  }.mkString("&")

  private def decodeForm(content: String) = new Form(content
    .split("&")
    .filter(_.contains("="))
    .map(nvp => (decode(nvp.split("=")(0), "UTF-8"), decode(nvp.split("=")(1), "UTF-8")))
    .groupBy(_._1)
    .mapValues(_.map(_._2))
    .mapValues(_.toSet))
}