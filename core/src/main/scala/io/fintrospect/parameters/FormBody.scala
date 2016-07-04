package io.fintrospect.parameters

import java.net.URLDecoder.decode
import java.net.URLEncoder.encode

import com.twitter.finagle.http.Message
import io.fintrospect.ContentTypes.APPLICATION_FORM_URLENCODED
import io.fintrospect.parameters.AbstractFormBody.{decodeForm, decodeWebForm, encodeForm}
import io.fintrospect.util.HttpRequestResponseUtil.contentFrom
import org.jboss.netty.handler.codec.http.HttpHeaders.Names

import scala.util.{Failure, Success, Try}

protected abstract class AbstractFormBody[T](fields: Seq[FormField[_]], deserialize: String => T, serialize: T => String)
  extends Body[T](new BodySpec[T](None, APPLICATION_FORM_URLENCODED, deserialize, serialize))
  with Bindable[T, Binding] {

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
  * Forms are modelled as a collection of fields.
  */
class FormBody(fields: Seq[FormField[_] with Retrieval[Form, _] with Extractor[Form, _]])
  extends AbstractFormBody[Form](fields, decodeForm, encodeForm)
  with MandatoryRebind[Message, Form, Binding] {

  override def <--?(message: Message): Extraction[Form] =
    Try(spec.deserialize(contentFrom(message))) match {
      case Success(form) =>
        Extraction.combine(fields.map(_.extract(form))) match {
          case failed@ExtractionFailed(_) => failed
          case _ => Extracted(form)
        }
      case Failure(e) => ExtractionFailed(fields.filter(_.required).map(InvalidParameter(_, "Could not parse")))
    }
}

/**
  * Forms are effectively modelled as a collection of fields.
  */
class WebFormBody(fields: Seq[FormField[_] with Retrieval[WebForm, _] with Extractor[WebForm, _]])
  extends AbstractFormBody[WebForm](fields, decodeWebForm, encodeForm) {

  override def <--?(message: Message): Extraction[WebForm] = Try(spec.deserialize(contentFrom(message))) match {
    case Success(form) => ???
    case Failure(e) => ExtractionFailed(fields.filter(_.required).map(InvalidParameter(_, "Could not parse")))
  }
}

object AbstractFormBody {
  def encodeForm(form: Form): String = form.flatMap {
    case (name, values) => values.map {
      case value => encode(name, "UTF-8") + "=" + encode(value, "UTF-8")
    }
  }.mkString("&")

  def decodeWebForm(content: String) = new WebForm(decodeFields(content), Nil)

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
