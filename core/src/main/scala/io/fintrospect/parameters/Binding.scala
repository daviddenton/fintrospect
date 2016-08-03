package io.fintrospect.parameters

import com.twitter.finagle.http.Request
import io.fintrospect.RequestBuilder

/**
  * Represents the binding of a parameter to it's value in a particular context
  */
sealed trait Binding {
  val parameter: Parameter

  def apply(requestBuild: RequestBuilder): RequestBuilder
}

class QueryBinding(val parameter: Parameter, value: String) extends Binding {
  def apply(requestBuild: RequestBuilder) = {
    val newValues = value +: requestBuild.queries.getOrElse(parameter.name, Nil)
    requestBuild.copy(queries = requestBuild.queries + (parameter.name -> newValues))
  }
}

class PathBinding(val parameter: Parameter, value: String) extends Binding {
  def apply(requestBuild: RequestBuilder) = requestBuild.copy(uriParts = requestBuild.uriParts :+ value)
}

class RequestBinding(val parameter: Parameter, into: Request => Request) extends Binding {
  def apply(requestBuild: RequestBuilder) = requestBuild.copy(fn = requestBuild.fn.andThen(into))
}

class FormFieldBinding(parameter: Parameter, value: String) extends RequestBinding(parameter, identity) {
  def apply(form: Form) = form +(parameter.name, value)
}

