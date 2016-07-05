package io.fintrospect.parameters

sealed trait InvalidParameter {
  val param: Parameter
  val reason: String

  override def toString = s"$reason:$param"
}

object InvalidParameter {
  def apply(param: Parameter, reason: String) = Custom(param, reason)

  case class Custom(param: Parameter, reason: String) extends InvalidParameter

  case class Unknown(param: Parameter) extends InvalidParameter {
    val reason = "Unknown"
  }

  case class Missing(param: Parameter) extends InvalidParameter {
    val reason = "Missing"
  }

  case class Invalid(param: Parameter) extends InvalidParameter {
    val reason = "Invalid"
  }

}