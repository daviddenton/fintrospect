package io.github.daviddenton.fintrospect

import io.github.daviddenton.fintrospect.parameters.{RequestParameter, Requirement, OptionalRequestParameter, RequiredRequestParameter}
import org.jboss.netty.handler.codec.http.HttpResponseStatus

case class Description private(name: String,
                               summary: Option[String],
                               produces: List[MimeType],
                               consumes: List[MimeType],
                               optional: List[OptionalRequestParameter[_]],
                               required: List[RequiredRequestParameter[_]],
                               responses: Map[HttpResponseStatus, String]) {
  def consuming(mimeType: MimeType*) = copy(consumes = (mimeType ++ produces).toList)

  def producing(mimeType: MimeType*) = copy(produces = (mimeType ++ produces).toList)
  def taking(rp: RequiredRequestParameter[_]) = copy(required = rp :: required)
  def taking(rp: OptionalRequestParameter[_]) = copy(optional = rp :: optional)

  def returning(codes: (HttpResponseStatus, String)*): Description = copy(responses = responses ++ codes)

  def allParams: Iterable[(Requirement, RequestParameter[_])] = List[(Requirement, RequestParameter[_])]() ++ optional.map(Requirement.Optional -> _) ++ required.map(Requirement.Mandatory -> _)
}

object Description {
  def apply(name: String, summary: String = null): Description = Description(name, Option(summary), Nil, Nil, Nil, Nil, Map.empty)
}