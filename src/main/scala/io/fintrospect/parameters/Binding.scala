package io.fintrospect.parameters

import org.jboss.netty.handler.codec.http.HttpRequest

import scala.language.existentials

sealed trait Binding {
  val parameter: Parameter[_]
  def applyTo(requestBuild: RequestBuild): RequestBuild
}

class QueryBinding(val parameter: Parameter[_], key: String, value: String) extends Binding {
  def applyTo(requestBuild: RequestBuild) = requestBuild.copy(queries = requestBuild.queries + (key -> value))
}

class PathBinding(val parameter: Parameter[_], value: String) extends Binding {
  def applyTo(requestBuild: RequestBuild) = requestBuild.copy(uriParts = requestBuild.uriParts :+ value)
}

class RequestBinding(val parameter: Parameter[_], into: HttpRequest => HttpRequest) extends Binding {
  def applyTo(requestBuild: RequestBuild) = requestBuild.copy(fn = requestBuild.fn.andThen(into))
}

class FormFieldBinding(val parameter: Parameter[_], val key: String, val value: String) extends Binding {
  def applyTo(requestBuild: RequestBuild) = requestBuild
}

