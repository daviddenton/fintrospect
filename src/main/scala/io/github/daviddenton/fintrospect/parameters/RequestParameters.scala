package io.github.daviddenton.fintrospect.parameters

import io.github.daviddenton.fintrospect.parameters.Requirement._

class RequestParameters private(optional: List[RequestParameter[_]], required: List[RequestParameter[_]]) {
  def requiring(p: RequestParameter[_]) = new RequestParameters(optional, p :: required)

  def optionally(p: RequestParameter[_]) = new RequestParameters(p :: optional, required)

  def allParams: Iterable[(Requirement, RequestParameter[_])] = optional.map(Optional -> _) ++ required.map(Mandatory -> _)
}

object RequestParameters {
  def apply(): RequestParameters = new RequestParameters(Nil, Nil)
}