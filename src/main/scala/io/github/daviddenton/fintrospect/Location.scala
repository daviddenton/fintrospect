package io.github.daviddenton.fintrospect

object Location extends Enumeration {
  type Location = Value
  val header, body, query, path, param = Value
}
