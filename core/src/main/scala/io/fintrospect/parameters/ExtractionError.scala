package io.fintrospect.parameters

sealed trait ExtractionError[T] {
  val name: T
  val reason: String

  override def toString = s"$name:$reason"
}

object ExtractionError {
  def apply[T](name: T, reason: String) = Custom(name, reason)

  case class Custom[T](name: T, reason: String) extends ExtractionError[T]

  case class Missing[T](name: T) extends ExtractionError[T] {
    val reason = "Missing"
  }

  case class Invalid[T](name: T) extends ExtractionError[T] {
    val reason = "Invalid"
  }
}