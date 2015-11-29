package io.fintrospect.parameters

sealed trait Security

trait ApiKey extends Security {
  val param: Parameter
}

object NoSecurity extends Security