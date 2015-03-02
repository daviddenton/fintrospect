package io.github.daviddenton.fintrospect.swagger.v1dot1

object Location extends Enumeration {
  type Location = Value
  val header, body, query, path, param = Value
}
