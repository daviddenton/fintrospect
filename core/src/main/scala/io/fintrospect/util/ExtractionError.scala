package io.fintrospect.util

sealed trait ExtractionError {
  val name: String
  val reason: String

  override def toString = s"$name:$reason"
}

object ExtractionError {
  def apply(name: String, reason: String) = Custom(name, reason)

  case class Custom(name: String, reason: String) extends ExtractionError

  case class Missing(name: String) extends ExtractionError {
    val reason = "Missing"
  }

  case class Invalid(name: String) extends ExtractionError {
    val reason = "Invalid"
  }
}