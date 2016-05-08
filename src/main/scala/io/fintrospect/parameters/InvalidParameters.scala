package io.fintrospect.parameters

class InvalidParameters(invalid: Seq[Parameter]) extends Exception(
  invalid.flatMap(_.description).mkString(", ")
)
