package io.fintrospect.parameters

import org.jboss.netty.handler.codec.http.HttpRequest

import scala.language.existentials

sealed trait Binding {
  val parameter: Parameter

  def apply(requestBuild: RequestBuild): RequestBuild
}

class QueryBinding(val parameter: Parameter, value: String) extends Binding {
  def apply(requestBuild: RequestBuild) = {
    val newValues = value +: requestBuild.queries.getOrElse(parameter.name, Seq())
    requestBuild.copy(queries = requestBuild.queries + (parameter.name -> newValues))
  }
}

class PathBinding(val parameter: Parameter, value: String) extends Binding {
  def apply(requestBuild: RequestBuild) = requestBuild.copy(uriParts = requestBuild.uriParts :+ value)
}

class RequestBinding(val parameter: Parameter, into: HttpRequest => HttpRequest) extends Binding {
  def apply(requestBuild: RequestBuild) = requestBuild.copy(fn = requestBuild.fn.andThen(into))
}

class FormFieldBinding(val parameter: Parameter, value: String) extends Binding {
  def apply(requestBuild: RequestBuild) = requestBuild

  def apply(form: Form) = form +(parameter.name, Set(value))
}

