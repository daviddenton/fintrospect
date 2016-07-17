package io.fintrospect.parameters

import io.fintrospect.util.{ExtractionError, Validated, Validation, ValidationFailed}

/**
  * Basically a wrapper for a Form and a set of error fields in that form.
  */
case class WebForm(form: Form, errors: Seq[ExtractionError]) {

  def isValid = errors.isEmpty

  def validate(): Validation[Form] = if (isValid) Validated(form) else ValidationFailed(errors)
}
