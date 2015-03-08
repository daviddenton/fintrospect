package io.github.daviddenton.fintrospect

import io.github.daviddenton.fintrospect.parameters.RequestParameter
import org.jboss.netty.handler.codec.http.HttpResponseStatus

case class Description private(value: String, params: List[RequestParameter[_]], responses: Map[HttpResponseStatus, String]) {
  def requiring(rp: RequestParameter[_]) = copy(params = rp :: params)

  def returning(codes: (HttpResponseStatus, String)*): Description = copy(responses = responses ++ codes)
}

object Description {
  def apply(value: String): Description = Description(value, Nil, Map.empty)
}