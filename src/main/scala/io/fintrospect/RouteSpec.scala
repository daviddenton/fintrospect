package io.fintrospect

import argo.jdom.JsonRootNode
import io.fintrospect.parameters.{Body, HeaderParameter, QueryParameter}
import io.fintrospect.util.HttpRequestResponseUtil.contentFrom
import org.jboss.netty.handler.codec.http.{HttpMethod, HttpResponse, HttpResponseStatus}

/**
 * Encapsulates the specification of an HTTP endpoint, for use by either the
 */
case class RouteSpec private(summary: String,
                             produces: Set[ContentType],
                             consumes: Set[ContentType],
                             body: Option[Body[_]],
                             headerParams: Seq[HeaderParameter[_]],
                             queryParams: Seq[QueryParameter[_]],
                             responses: Seq[ResponseSpec]) {

  /**
   * Register content types which the route will consume. This is informational only and is NOT currently enforced.
   */
  def consuming(contentTypes: ContentType*): RouteSpec = copy(consumes = produces ++ contentTypes)

  /**
   * Register content types which thus route will produce. This is informational only and NOT currently enforced.
   */
  def producing(contentTypes: ContentType*): RouteSpec = copy(produces = produces ++ contentTypes)

  /**
   * Register a header parameter. Mandatory parameters are checked for each request, and a 400 returned if any are missing.
   */
  def taking(rp: HeaderParameter[_]): RouteSpec = copy(headerParams = rp +: headerParams)

  /**
   * Register a query parameter. Mandatory parameters are checked for each request, and a 400 returned if any are missing.
   */
  def taking(rp: QueryParameter[_]): RouteSpec = copy(queryParams = rp +: queryParams)

  /**
   * Register the expected content of the body.
   */
  def body(bp: Body[_]): RouteSpec = copy(body = Option(bp), consumes = consumes + bp.contentType)

  /**
   * Register a possible response which could be produced by this route, with an example JSON body (used for schema generation).
   */
  def returning(newResponse: ResponseSpec): RouteSpec = copy(responses = newResponse +: responses)

  /**
   * Register one or more possible responses which could be produced by this route.
   */
  def returning(codes: (HttpResponseStatus, String)*): RouteSpec = copy(responses = responses ++ codes.map(c => new ResponseSpec(c, null)))

  /**
   * Register an exact possible response which could be produced by this route. Will be used for schema generation if content is JSON.
   */
  def returning(response: HttpResponse): RouteSpec = {
    returning(new ResponseSpec(response.getStatus -> response.getStatus.getReasonPhrase, Option(contentFrom(response))))
  }

  /**
   * Register a possible response which could be produced by this route, with an example JSON body (used for schema generation).
   */
  def returning(code: (HttpResponseStatus, String), example: JsonRootNode): RouteSpec = copy(responses = ResponseSpec.json(code, example) +: responses)

  /**
   * Register a possible response which could be produced by this route, with an example body (used for schema generation).
   */
  def returning(code: (HttpResponseStatus, String), example: String): RouteSpec = copy(responses = new ResponseSpec(code, Option(example)) +: responses)

  def at(method: HttpMethod) = IncompletePath(this, method)
}

object RouteSpec {
  def apply(summary: String = "<unknown>"): RouteSpec = RouteSpec(summary, Set.empty, Set.empty, None, Nil, Nil, Nil)
}
