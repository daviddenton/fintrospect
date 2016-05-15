package io.fintrospect.parameters

case class InvalidParameter(param: Parameter, reason: String)

object InvalidParameter {
  def Missing(parameter: Parameter) = InvalidParameter(parameter, "Missing")
  def Invalid(parameter: Parameter) = InvalidParameter(parameter, "Invalid")
}