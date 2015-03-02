package io.github.daviddenton.fintrospect.swagger.v2

object Location extends Enumeration {
  type Location = Value
  val header, body, query, path, param = Value
}
