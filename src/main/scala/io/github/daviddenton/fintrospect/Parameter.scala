package io.github.daviddenton.fintrospect

import java.beans.Introspector._

import io.github.daviddenton.fintrospect.Location.Location

class Parameter private(val name: String, val location: Location, clazz: Class[_]) {
  val paramType = decapitalize(clazz.getSimpleName)
}

object Parameter {
  def path(name: String, clazz: Class[_]) = new Parameter(name, Location.path, clazz)

  def body(name: String, clazz: Class[_]) = new Parameter(name, Location.body, clazz)

  def query(name: String, clazz: Class[_]) = new Parameter(name, Location.query, clazz)

  def header(name: String, clazz: Class[_]) = new Parameter(name, Location.header, clazz)
}