package io.fintrospect.parameters

/**
  * Basically a wrapper for a Form and a set of error fields in that form.
  */
case class WebForm(form: Form, errors: Seq[ExtractionError[String]]) {

  def withErrors(newErrors: Seq[ExtractionError[String]]) = copy(errors = errors ++ newErrors)

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
