package io.fintrospect.parameters

import java.net.URLDecoder.decode
import java.net.URLEncoder.encode

import com.twitter.finagle.http.Message
import io.fintrospect.ContentTypes.APPLICATION_FORM_URLENCODED
import io.fintrospect.parameters.AbstractFormBody.{decodeForm, encodeForm}
import org.jboss.netty.handler.codec.http.HttpHeaders.Names

import scala.util.{Failure, Success, Try}

protected abstract class AbstractFormBody[T <: Form](fields: Seq[FormField[_]])
  extends Body[Form](new BodySpec[Form](None, APPLICATION_FORM_URLENCODED, decodeForm, encodeForm))
  with Bindable[T, Binding]
  with Mandatory[Message, T] {

  override def iterator = fields.iterator

  def -->(value: T): Seq[Binding] =
    Seq(new RequestBinding(null, t => {
      val content = spec.serialize(value)
      t.headerMap.add(Names.CONTENT_TYPE, spec.contentType.value)
      t.headerMap.add(Names.CONTENT_LENGTH, content.length.toString)
      t.setContentString(content)
      t
    })) ++ fields.map(f => new FormFieldBinding(f, ""))
}

/**
  * Forms are a collection of valid fields. Failure to extract a single field will result in the entire form failing.
  * This form is used for non-web forms (where the posted form is merely an url-encoded set of form parameters) and
  * will auto-reject requests with a BadRequest.
  */
class FormBody(fields: Seq[FormField[_] with Retrieval[Form, _] with Extractor[Form, _]])
  extends AbstractFormBody[Form](fields)
  with MandatoryRebind[Message, Form, Binding] {

  override def <--?(message: Message): Extraction[Form] =
    Try(spec.deserialize(message.contentString)) match {
      case Success(form) =>
        Extraction.combine(fields.map(_.extract(form))) match {
          case failed@ExtractionFailed(_) => failed
          case _ => Extracted(form)
        }
      case Failure(e) => ExtractionFailed(fields.filter(_.required).map(InvalidParameter(_, "Could not parse")))
    }
}

/**
  * Web-forms are a collection of valid and invalid fields.
  * This form is used for web forms (where feedback is desirable and the user can be redirected back to the form page.
  */
class WebFormBody(fields: Seq[FormField[_] with Retrieval[Form, _] with Extractor[Form, _]])
  extends AbstractFormBody[WebForm](fields) {

  override def <--?(message: Message): Extraction[WebForm] =
    Try(spec.deserialize(message.contentString)) match {
      case Success(form) =>
        Extracted(new WebForm(form.fields, fields.map(_.extract(form)).flatMap {
          case Extracted(_) => Nil
          case NotProvided => Nil
          case ExtractionFailed(e) => e
        }))
      case Failure(e) => ExtractionFailed(fields.filter(_.required).map(InvalidParameter(_, "Could not parse")))
    }
}

protected object AbstractFormBody {
  private def encodeForm(form: Form): String = form.flatMap {
    case (name, values) => values.map {
      case value => encode(name, "UTF-8") + "=" + encode(value, "UTF-8")
    }
  }.mkString("&")

  private def decodeForm(content: String) = new Form(decodeFields(content))

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
