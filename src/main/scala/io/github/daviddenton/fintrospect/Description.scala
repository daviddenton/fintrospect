package io.github.daviddenton.fintrospect

import argo.jdom.JsonNode
import io.github.daviddenton.fintrospect.parameters.{Body, RequestParameter}
import io.github.daviddenton.fintrospect.util.ArgoUtil._
import io.github.daviddenton.fintrospect.util.ResponseBuilder
import org.jboss.netty.handler.codec.http.HttpResponseStatus

import scala.util.Try

case class Description private(name: String,
                               summary: Option[String],
                               produces: List[MimeType],
                               consumes: List[MimeType],
                               body: Option[Body],
                               params: List[RequestParameter[_]],
                               responses: List[ResponseWithExample]) {
  def consuming(mimeType: MimeType*) = copy(consumes = (mimeType ++ produces).toList)

  def producing(mimeType: MimeType*) = copy(produces = (mimeType ++ produces).toList)

  def taking(rp: RequestParameter[_]) = copy(params = rp :: params)

  def taking(bp: Body) = copy(body = Some(bp))

  def returning(newResponse: ResponseWithExample): Description = copy(responses = newResponse :: responses)

  def returning(codes: (HttpResponseStatus, String)*): Description = copy(responses = responses ++ codes.map(c => ResponseWithExample(c._1, c._2)))

  def returning(responseBuilder: ResponseBuilder): Description = {
    val response = responseBuilder.build
    returning(ResponseWithExample(response.getStatus(), response.getStatus().getReasonPhrase, Try(parse(response.contentString)).getOrElse(nullNode())))
  }

  def returning(code: (HttpResponseStatus, String), example: JsonNode): Description = copy(responses = ResponseWithExample(code._1, code._2, example) :: responses)
}

object Description {
  def apply(name: String, summary: String = null): Description = Description(name, Option(summary), Nil, Nil, None, Nil, Nil)
}