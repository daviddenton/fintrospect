package io.fintrospect.parameters

import com.twitter.finagle.httpx.Request

import scala.language.existentials

/**
 * Represents the binding of a parameter to it's value in a particular context
 */
sealed trait Binding {
  val parameter: Parameter

  def apply(requestBuild: RequestBuild): RequestBuild
}

class QueryBinding(val parameter: Parameter, value: String) extends Binding {
  def apply(requestBuild: RequestBuild) = {
    val newValues = value +: requestBuild.queries.getOrElse(parameter.name, Nil)
    requestBuild.copy(queries = requestBuild.queries + (parameter.name -> newValues))
  }
}

class PathBinding(val parameter: Parameter, value: String) extends Binding {
  def apply(requestBuild: RequestBuild) = requestBuild.copy(uriParts = requestBuild.uriParts :+ value)
}

class RequestBinding(val parameter: Parameter, into: Request => Request) extends Binding {
  def apply(requestBuild: RequestBuild) = requestBuild.copy(fn = requestBuild.fn.andThen(into))
}

class FormFieldBinding(val parameter: Parameter, value: String) extends Binding {
  def apply(requestBuild: RequestBuild) = requestBuild

  def apply(form: Form) = form +(parameter.name, value)
}

