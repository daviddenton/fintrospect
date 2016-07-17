package io.fintrospect.parameters

import io.fintrospect.util.ExtractionError

/**
  * Basically a wrapper for a Form and a set of error fields in that form.
  */
class WebForm(val form: Form, val errors: Seq[ExtractionError]) {

  def isValid = errors.isEmpty

  def <--[A](fieldA: Retrieval[Form, A]): A = fieldA <-- form

}

object WebForm {
  val empty = new WebForm(Form(), Nil)
}
