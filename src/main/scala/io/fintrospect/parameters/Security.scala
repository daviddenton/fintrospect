package io.fintrospect.parameters

sealed trait Security

case class ApiKey(param: Parameter) extends Security

object NoSecurity extends Security