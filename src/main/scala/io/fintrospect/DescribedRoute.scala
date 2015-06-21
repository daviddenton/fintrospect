package io.fintrospect

import argo.jdom.JsonNode
import io.fintrospect.parameters.{Body, RequestParameter}
import io.fintrospect.util.ArgoUtil._
import io.fintrospect.util.HttpRequestResponseUtil.contentFrom
import org.jboss.netty.handler.codec.http.{HttpMethod, HttpResponse, HttpResponseStatus}

import scala.util.Try

/**
 * Encapsulates the description of a route.
 */
case class DescribedRoute private(summary: String,
                                  produces: Set[ContentType],
                                  consumes: Set[ContentType],
                                  body: Option[Body[_]],
                                  requestParams: Seq[RequestParameter[_]],
                                  responses: Seq[ResponseWithExample]) {

  /**
   * Register content types which the route will consume. This is informational only and is NOT currently enforced.
   */
  def consuming(contentTypes: ContentType*): DescribedRoute = copy(consumes = produces ++ contentTypes)

  /**
   * Register content types which thus route will produce. This is informational only and NOT currently enforced.
   */
  def producing(contentTypes: ContentType*): DescribedRoute = copy(produces = produces ++ contentTypes)

  /**
   * Register a request parameter. Mandatory parameters are checked for each request, and a 400 returned if any are missing.
   */
  def taking(rp: RequestParameter[_]): DescribedRoute = copy(requestParams = rp +: requestParams)

  /**
   * Register the expected content of the body.
   */
  def body(bp: Body[_]): DescribedRoute = copy(body = Some(bp), consumes = consumes + bp.contentType)

  /**
   * Register a possible response which could be produced by this route, with an example JSON body (used for schema generation).
   */
  def returning(newResponse: ResponseWithExample): DescribedRoute = copy(responses = newResponse +: responses)

  /**
   * Register one or more possible responses which could be produced by this route.
   */
  def returning(codes: (HttpResponseStatus, String)*): DescribedRoute = copy(responses = responses ++ codes.map(c => ResponseWithExample(c._1, c._2)))

  /**
   * Register an exact possible response which could be produced by this route. Will be used for schema generation if content is JSON.
   */
  def returning(response: HttpResponse): DescribedRoute = {
    returning(ResponseWithExample(response.getStatus, response.getStatus.getReasonPhrase, Try(parse(contentFrom(response))).getOrElse(nullNode())))
  }

  /**
   * Register a possible response which could be produced by this route, with an example JSON body (used for schema generation).
   */
  def returning(code: (HttpResponseStatus, String), example: JsonNode): DescribedRoute = copy(responses = ResponseWithExample(code._1, code._2, example) +: responses)

  def at(method: HttpMethod) = IncompletePath(this, method)
}

object DescribedRoute {
  def apply(summary: String): DescribedRoute = DescribedRoute(summary, Set.empty, Set.empty, None, Nil, Nil)
}
