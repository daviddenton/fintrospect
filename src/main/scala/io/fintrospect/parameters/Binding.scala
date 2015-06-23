package io.fintrospect.parameters

import org.jboss.netty.handler.codec.http.HttpRequest

import scala.language.existentials

sealed trait Binding

case class QueryBinding(entry: (String, String)) extends Binding

case class PathBinding(parameter: Parameter[_], value: String) extends Binding

case class RequestBinding(into: HttpRequest => HttpRequest) extends Binding

