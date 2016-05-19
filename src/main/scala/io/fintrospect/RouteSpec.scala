package io.fintrospect

import com.twitter.finagle.http.{Method, Request, Response, Status}
import io.fintrospect.formats.json.{Argo, JsonFormat}
import io.fintrospect.parameters.{Body, HeaderParameter, Parameter, QueryParameter, Extractable}
import io.fintrospect.util.HttpRequestResponseUtil.contentFrom

/**
 * Encapsulates the specification of an HTTP endpoint, for use by either a Finagle server or client.
 */
case class RouteSpec private(summary: String,
                             description: Option[String],
                             produces: Set[ContentType],
                             consumes: Set[ContentType],
                             body: Option[Body[_]],
                             requestParams: Seq[Parameter with Extractable[Request, _]],
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
  def taking(rp: HeaderParameter[_] with Extractable[Request, _]): RouteSpec = copy(requestParams = rp +: requestParams)

  /**
   * Register a query parameter. Mandatory parameters are checked for each request, and a 400 returned if any are missing.
   */
  def taking(rp: QueryParameter[_] with Extractable[Request, _]): RouteSpec = copy(requestParams = rp +: requestParams)

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
  def returning(codes: (Status, String)*): RouteSpec = copy(responses = responses ++ codes.map(c => new ResponseSpec(c, null)))

  /**
   * Register an exact possible response which could be produced by this route. Will be used for schema generation if content is JSON.
   */
  def returning(response: Response): RouteSpec = {
    returning(new ResponseSpec(response.status -> response.status.reason, Option(contentFrom(response))))
  }

  /**
   * Register a possible response which could be produced by this route, with an example JSON body (used for schema generation).
   */
  def returning[T](code: (Status, String), example: T, jsonFormat: JsonFormat[T, _] = Argo.JsonFormat): RouteSpec = copy(responses = ResponseSpec.json(code, example, jsonFormat) +: responses)

  /**
   * Register a possible response which could be produced by this route, with an example body (used for schema generation).
   */
  def returning(code: (Status, String), example: String): RouteSpec = copy(responses = new ResponseSpec(code, Option(example)) +: responses)

  def at(method: Method) = IncompletePath(this, method)
}

object RouteSpec {
  def apply(summary: String = "<unknown>", description: String = null): RouteSpec =
    RouteSpec(summary, Option(description), Set.empty, Set.empty, None, Nil, Nil)
}
