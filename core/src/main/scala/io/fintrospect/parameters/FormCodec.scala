package io.fintrospect.parameters

import java.net.{URLDecoder, URLEncoder}

import io.fintrospect.util.{Extracted, Extraction, ExtractionError, ExtractionFailed, Extractor}

/**
  * Represents different strategies for decoding and encoding HTML forms.
  */
trait FormCodec[T] {

  protected def decodeFields(content: String): Map[String, Set[String]] = {
    content
      .split("&")
      .filter(_.contains("="))
      .map(nvp => {
        val parts = nvp.split("=")
        (URLDecoder.decode(parts(0), "UTF-8"), if (parts.length > 1) URLDecoder.decode(parts(1), "UTF-8") else "")
      })
      .groupBy(_._1)
      .mapValues(_.map(_._2))
      .mapValues(_.toSet)
  }

  def encode(form: Form): String = form.flatMap {
    case (name, values) => values.map(value => URLEncoder.encode(name, "UTF-8") + "=" + URLEncoder.encode(value, "UTF-8"))
  }.mkString("&")

  def decode(fields: Seq[Extractor[Form, _]], s: String): T

  def extract(fields: Seq[Extractor[Form, _]], f: T): Extraction[T]
}



/**
  * Web-forms are a less harsh version of forms, which report both a collection of received fields and a set of invalid fields.
  * This form-type is to be used for web forms (where feedback is desirable and the user can be redirected back to the form page).
  * As such, extracting an invalid webform from a request will not fail unless the body encoding itself is invalid.
  */
class WebFormCodec(messages: Map[Parameter, String]) extends FormCodec[Form] {
  override def decode(fields: Seq[Extractor[Form, _]], s: String): Form = {
    val rawForm = new Form(decodeFields(s), Nil)
    new Form(rawForm.fields, fields.flatMap {
      _ <--? rawForm match {
        case ExtractionFailed(e) => e.map(er => ExtractionError(er.param, messages.getOrElse(er.param, er.reason)))
        case _ => Nil
      }
    })
  }

  override def extract(fields: Seq[Extractor[Form, _]], t: Form): Extraction[Form] = Extracted(Some(t))
}

/**
  * Strict Forms fail when failing even a single field fails.
  * This form is used for non-web forms (where the posted form is merely an url-encoded set of form parameters) and
  * will auto-reject requests with a BadRequest.
  */
class StrictFormCodec() extends FormCodec[Form] {
  override def decode(fields: Seq[Extractor[Form, _]], s: String): Form = new Form(decodeFields(s), Nil)

  override def extract(fields: Seq[Extractor[Form, _]], form: Form): Extraction[Form] = Extraction.combine(fields.map(_.extract(form))) match {
    case failed@ExtractionFailed(_) => failed
    case _ => Extracted(Some(form))
  }
}
