package io.github.daviddenton.fintrospect

import io.github.daviddenton.fintrospect.parameters.RequestParameter
import org.jboss.netty.handler.codec.http.HttpResponseStatus

case class Description private(name: String,
                               summary: Option[String],
                               produces: List[String],
                               consumes: List[String],
                               params: List[RequestParameter[_]],
                               responses: Map[HttpResponseStatus, String]) {
  def consuming(mimeType: String*) = copy(consumes = (mimeType ++ produces).toList)

  def producing(mimeType: String*) = copy(produces = (mimeType ++ produces).toList)

  def requiring(rp: RequestParameter[_]) = copy(params = rp :: params)

  def returning(codes: (HttpResponseStatus, String)*): Description = copy(responses = responses ++ codes)
}

object Description {
  def apply(name: String, summary: String = null): Description = Description(name, Option(summary), Nil, Nil, Nil, Map.empty)
}