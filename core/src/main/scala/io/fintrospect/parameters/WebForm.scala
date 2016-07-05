package io.fintrospect.parameters

/**
  * Basically a wrapper for a Form and a set of error fields in that form.
  */
case class WebForm(form: Form, errors: Iterable[InvalidParameter]) {
  def isValid = errors.isEmpty
}
