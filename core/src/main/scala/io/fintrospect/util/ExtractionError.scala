package io.fintrospect.util

import io.fintrospect.parameters.Parameter

sealed trait ExtractionError {
  val param: Parameter
  val reason: String

  override def toString = s"${param.name}:$reason"
}

object ExtractionError {
  def apply(param: Parameter, reason: String) = Custom(param, reason)

  case class Custom(param: Parameter, reason: String) extends ExtractionError

  case class Missing(param: Parameter) extends ExtractionError {
    val reason = "Missing"
  }

  case class Invalid(param: Parameter) extends ExtractionError {
    val reason = "Invalid"
  }
}