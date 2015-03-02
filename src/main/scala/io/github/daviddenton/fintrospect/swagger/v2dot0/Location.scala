package io.github.daviddenton.fintrospect.swagger.v2dot0

object Location extends Enumeration {
  type Location = Value
  val header, body, query, path, param = Value
}
