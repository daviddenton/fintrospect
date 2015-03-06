package io.github.daviddenton.fintrospect

import java.beans.Introspector._

class Parameter private(val name: String, val location: String, val required: Boolean, clazz: Class[_]) {
  val paramType = decapitalize(clazz.getSimpleName)
}

object Parameter {
  def path(name: String, clazz: Class[_]) = new Parameter(name, "path", true, clazz)

  def body(name: String, clazz: Class[_]) = new Parameter(name, "body", true, clazz)

  def query(name: String, clazz: Class[_]) = new Parameter(name, "query", true, clazz)

  def header(name: String, clazz: Class[_]) = new Parameter(name, "header", true, clazz)
}