package io.fintrospect.parameters

import io.fintrospect.util._

/**
  * A strategy for validating the fields in a form
  */
sealed trait FormValidator {
  def apply(fields: Seq[Extractor[Form, _]], form: Form): Form
}

/**
  * Web-forms are a less harsh version of forms, which report both a collection of received fields and a set of invalid fields.
  * This form-type is to be used for web forms (where feedback is desirable and the user can be redirected back to the form page).
  * As such, extracting an invalid webform from a request will not fail unless the body encoding itself is invalid.
  */
class WebFormValidator(messages: Map[Parameter, String]) extends FormValidator {
  override def apply(fields: Seq[Extractor[Form, _]], rawForm: Form): Form =
    new Form(rawForm.fields, rawForm.files, fields.flatMap {
      _ <--? rawForm match {
        case ExtractionFailed(e) => e.map(er => ExtractionError(er.param, messages.getOrElse(er.param, er.reason)))
        case _ => Nil
      }
    })
}

/**
  * Strict Forms fail when failing even a single field fails.
  * This form is used for non-web forms (where the posted form is merely an url-encoded set of form parameters) and
  * will auto-reject requests with a BadRequest.
  */
object StrictFormValidator extends FormValidator {
  override def apply(fields: Seq[Extractor[Form, _]], rawForm: Form): Form = rawForm
}
