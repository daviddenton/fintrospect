package io.github.daviddenton.fintrospect

import java.beans.Introspector._

class Parameter private(val name: String, val location: String, clazz: Class[_]) {
  val paramType = decapitalize(clazz.getSimpleName)
}

object Parameter {
  def path(name: String, clazz: Class[_]) = new Parameter(name, "path", clazz)

  def body(name: String, clazz: Class[_]) = new Parameter(name, "body", clazz)

  def query(name: String, clazz: Class[_]) = new Parameter(name, "query", clazz)

  def header(name: String, clazz: Class[_]) = new Parameter(name, "header", clazz)
}