package io.fintrospect.parameters

import io.fintrospect.util.ExtractionError

/**
  * Basically a wrapper for a Form and a set of error fields in that form.
  */
class WebForm(fields: Map[String, Set[String]], val errors: Seq[ExtractionError]) extends Form(fields) {

  def isValid = errors.isEmpty

}

object WebForm {
  val empty = new WebForm(Map(), Nil)
}
