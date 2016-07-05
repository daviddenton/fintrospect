package io.fintrospect.parameters

/**
  * Basically a wrapper for a Form and a set of error fields in that form.
  */
case class WebForm(form: Form, errors: Seq[InvalidParameter]) {

  def withErrors(newErrors: Seq[InvalidParameter]) = copy(errors = errors ++ newErrors)
  def isValid = errors.isEmpty

  def validate[T <: Product](fn: (Form => Validator[T])): Validator[T] = fn(form)
}
