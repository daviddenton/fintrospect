package io.github.daviddenton.fintrospect

import argo.jdom.JsonNode
import io.github.daviddenton.fintrospect.parameters.{Body, RequestParameter}
import io.github.daviddenton.fintrospect.util.ArgoUtil._
import io.github.daviddenton.fintrospect.util.ResponseBuilder
import org.jboss.netty.handler.codec.http.HttpResponseStatus

import scala.util.Try

/**
 * Encapsulates the description of a route.
 */
case class Description private(name: String,
                               summary: Option[String],
                               produces: List[MimeType],
                               consumes: List[MimeType],
                               body: Option[Body],
                               params: List[RequestParameter[_]],
                               responses: List[ResponseWithExample]) {

  /**
   * Register mime types which the route will consume. This is informational only and is NOT currently enforced.
   */
  def consuming(mimeType: MimeType*) = copy(consumes = (mimeType ++ produces).toList)

  /**
   * Register mime types which thus route will produce. This is informational only and not currently enforced.
   */
  def producing(mimeType: MimeType*) = copy(produces = (mimeType ++ produces).toList)

  /**
   * Register a request parameter. Mandatory parameters are checked for each request, and a 400 returned if any are missing.
   */
  def taking(rp: RequestParameter[_]) = copy(params = rp :: params)

  /**
   * Register the expected content of the body. Presence is NOT currently enforced.
   */
  def taking(bp: Body) = copy(body = Some(bp))

  /**
   * Register a possible response which could be produced by this route, with an example JSON body (used for schema generation).
   */
  def returning(newResponse: ResponseWithExample): Description = copy(responses = newResponse :: responses)

  /**
   * Register one or more possible responses which could be produced by this route.
   */
  def returning(codes: (HttpResponseStatus, String)*): Description = copy(responses = responses ++ codes.map(c => ResponseWithExample(c._1, c._2)))

  /**
   * Register an exact possible response which could be produced by this route. Will be used for schema generation if content is JSON.
   */
  def returning(responseBuilder: ResponseBuilder): Description = {
    val response = responseBuilder.build
    returning(ResponseWithExample(response.getStatus(), response.getStatus().getReasonPhrase, Try(parse(response.contentString)).getOrElse(nullNode())))
  }

  /**
   * Register a possible response which could be produced by this route, with an example JSON body (used for schema generation).
   */
  def returning(code: (HttpResponseStatus, String), example: JsonNode): Description = copy(responses = ResponseWithExample(code._1, code._2, example) :: responses)
}

object Description {
  def apply(name: String, summary: String = null): Description = Description(name, Option(summary), Nil, Nil, None, Nil, Nil)
}