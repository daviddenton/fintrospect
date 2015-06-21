package io.fintrospect.parameters

import scala.language.existentials

sealed trait Validation {
  val success: Boolean
  val param: Parameter[_]
}

case class ParamOk(param: Parameter[_]) extends Validation {
  val success = true
}

case class Missing(param: Parameter[_]) extends Validation {
  val success = false
}

case class Malformed(param: Parameter[_]) extends Validation {
  val success = false
}