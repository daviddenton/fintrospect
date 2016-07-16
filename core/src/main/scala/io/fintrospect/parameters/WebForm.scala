package io.fintrospect.parameters

import io.fintrospect.util.{ExtractionError, Validated, Validation, ValidationFailed, Validator}

/**
  * Basically a wrapper for a Form and a set of error fields in that form.
  */
case class WebForm(form: Form, errors: Seq[ExtractionError]) {

  def withErrors(newErrors: Seq[ExtractionError]) = copy(errors = errors ++ newErrors)

  def isValid = errors.isEmpty

  def validate[T <: Product](fn: (Form => Validator[T])): Validation[T] =
    if (isValid)
      fn(form) {
        case t => Validated(t)
      } match {
        case Validated(t) => Validated(t.value)
        case ValidationFailed(newErrors) => ValidationFailed(newErrors)
      } else {
      ValidationFailed(errors)
    }
}
