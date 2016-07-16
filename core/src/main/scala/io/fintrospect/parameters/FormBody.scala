package io.fintrospect.parameters

import java.net.URLDecoder.decode
import java.net.URLEncoder.encode

import com.twitter.finagle.http.Message
import io.fintrospect.ContentTypes.APPLICATION_FORM_URLENCODED
import io.fintrospect.util.{Extracted, Extraction, ExtractionError, ExtractionFailed, Extractor}
import org.jboss.netty.handler.codec.http.HttpHeaders.Names

import scala.util.{Failure, Success, Try}

/**
  * Forms are a collection of valid fields. Failure to extract a single field will result in the entire form failing.
  * This form is used for non-web forms (where the posted form is merely an url-encoded set of form parameters) and
  * will auto-reject requests with a BadRequest.
  */
class FormBody(val fields: Seq[FormField[_] with Retrieval[Form, _] with Extractor[Form, _]])
  extends Body[Form](new BodySpec[Form](None, APPLICATION_FORM_URLENCODED, FormBody.decodeForm, FormBody.encodeForm))
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
      case Success(form) =>
        Extraction.combine(fields.map(_.extract(form))) match {
          case failed@ExtractionFailed(_) => failed
          case _ => Extracted(Some(form))
        }
      case Failure(e) => ExtractionFailed(fields.filter(_.required).map(s => ExtractionError(s.name, "Could not parse")))
    }
}



protected object FormBody {
  def encodeForm(form: Form): String = form.flatMap {
    case (name, values) => values.map {
      case value => encode(name, "UTF-8") + "=" + encode(value, "UTF-8")
    }
  }.mkString("&")

  def decodeForm(content: String) = new Form(decodeFields(content))

  private def decodeFields(content: String): Map[String, Set[String]] = {
    content
      .split("&")
      .filter(_.contains("="))
      .map(nvp => {
        val parts = nvp.split("=")
        (decode(parts(0), "UTF-8"), if (parts.length > 1) decode(parts(1), "UTF-8") else "")
      })
      .groupBy(_._1)
      .mapValues(_.map(_._2))
      .mapValues(_.toSet)
  }
}
