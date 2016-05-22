package io.fintrospect.parameters

case class InvalidParameter(param: Parameter, reason: String) {
  override def toString = s"$reason:$param"
}

object InvalidParameter {
  def Unknown(parameter: Parameter) = InvalidParameter(parameter, "Unknown")
  def Missing(parameter: Parameter) = InvalidParameter(parameter, "Missing")
  def Invalid(parameter: Parameter) = InvalidParameter(parameter, "Invalid")
}