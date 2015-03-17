package io.github.daviddenton.fintrospect

import io.github.daviddenton.fintrospect.parameters.{RequestParameters, RequestParameter}
import org.jboss.netty.handler.codec.http.HttpResponseStatus

case class Description private(name: String,
                               summary: Option[String],
                               produces: List[MimeType],
                               consumes: List[MimeType],
                               requestParams: RequestParameters,
                               responses: Map[HttpResponseStatus, String]) {
  def consuming(mimeType: MimeType*) = copy(consumes = (mimeType ++ produces).toList)

  def producing(mimeType: MimeType*) = copy(produces = (mimeType ++ produces).toList)

  def requiring(rp: RequestParameter[_]) = copy(requestParams = requestParams.requiring(rp))
  def optionally(rp: RequestParameter[_]) = copy(requestParams = requestParams.optionally(rp))

  def returning(codes: (HttpResponseStatus, String)*): Description = copy(responses = responses ++ codes)
}

object Description {
  def apply(name: String, summary: String = null): Description = Description(name, Option(summary), Nil, Nil, RequestParameters(), Map.empty)
}