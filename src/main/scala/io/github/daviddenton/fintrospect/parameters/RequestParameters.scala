package io.github.daviddenton.fintrospect.parameters

import io.github.daviddenton.fintrospect.parameters.Requirement._

class RequestParameters private(optional: List[OptionalRequestParameter[_]], required: List[RequiredRequestParameter[_]]) {
  def requiring(p: RequiredRequestParameter[_]) = new RequestParameters(optional, p :: required)

  def optionally(p: OptionalRequestParameter[_]) = new RequestParameters(p :: optional, required)

  def allParams: Iterable[(Requirement, RequestParameter[_])] = List[(Requirement, RequestParameter[_])]() ++ optional.map(Optional -> _) ++ required.map(Mandatory -> _)
}

object RequestParameters {
  def apply(): RequestParameters = new RequestParameters(Nil, Nil)
}