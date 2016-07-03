package io.fintrospect.parameters

sealed trait StringValidation extends (String => String)

object StringValidation {
  val EmptyIsValid = new StringValidation {
    override def apply(in: String): String = in.toString
  }
  val EmptyIsInvalid = new StringValidation {
    override def apply(in: String): String = if (in.isEmpty) throw new IllegalArgumentException("Cannot be empty") else in.toString
  }
}
