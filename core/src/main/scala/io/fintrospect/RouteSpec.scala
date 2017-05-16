package io.fintrospect

import com.twitter.finagle.http.{Method, Request, Response, Status}
import io.fintrospect.RouteSpec.QueryOrHeader
import io.fintrospect.formats.{Argo, JsonLibrary}
import io.fintrospect.parameters._
import io.fintrospect.util.{Extracted, Extraction, ExtractionFailed, Extractor}

/**
  * Encapsulates the specification of an HTTP endpoint, for use by either a Finagle server or client.
  */
case class RouteSpec private(summary: String,
                             description: Option[String],
                             produces: Set[ContentType],
                             consumes: Set[ContentType],
                             body: Option[Body[_]],
                             requestParams: Seq[QueryOrHeader[_]],
                             responses: Seq[ResponseSpec],
                             tags: Seq[TagInfo]) {

  private[fintrospect] def <--?(request: Request): Extraction[Request] = {
    val contents = Map[Any, Extraction[_]]((requestParams ++ body).map(r => (r, r <--? request)): _*)
    Extraction.combine(contents.values.toSeq).map(_ => ExtractedRouteRequest(request, contents))
  }

  /**
    * Register content types which the route will consume. This is informational only and is NOT currently enforced.
    */
  def consuming(contentTypes: ContentType*): RouteSpec = copy(consumes = consumes ++ contentTypes)

  /**
    * Register content types which thus route will produce. This is informational only and NOT currently enforced.
    */
  def producing(contentTypes: ContentType*): RouteSpec = copy(produces = produces ++ contentTypes)

  /**
    * Register a header/query parameter. Mandatory parameters are checked for each request, and a 400 returned if any are missing.
    */
  def taking(rp: QueryOrHeader[_]): RouteSpec = copy(requestParams = rp +: requestParams)

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
  def returning(response: Response): RouteSpec = returning(new ResponseSpec(response.status -> response.status.reason, Option(response.contentString)))

  /**
    * Register a possible response which could be produced by this route, with an example JSON body (used for schema generation).
    */
  def returning[T](code: (Status, String), example: T, jsonLib: JsonLibrary[T, _] = Argo): RouteSpec = copy(responses = ResponseSpec.json(code, example, jsonLib) +: responses)

  /**
    * Register a possible response which could be produced by this route, with an example body (used for schema generation).
    */
  def returning(code: (Status, String), example: String): RouteSpec = copy(responses = new ResponseSpec(code, Option(example)) +: responses)

  /**
    * Add tags to this routes. Provides the ability to group routes by tag in a generated schema.
    */
  def taggedWith(tag: String): RouteSpec = copy(tags = tags :+ TagInfo(tag))

  /**
    * Add tags to this routes. Provides the ability to group routes by tag in a generated schema.
    */
  def taggedWith(tags: TagInfo*): RouteSpec = copy(tags = this.tags ++ tags)

  def at(method: Method) = UnboundRoute(this, method)
}

object RouteSpec {
  type QueryOrHeader[T] = HasParameters with Extractor[Request, _] with Rebindable[Request, _, Binding]

  def apply(summary: String = "<unknown>", description: String = null): RouteSpec =
    RouteSpec(summary, Option(description), Set.empty, Set.empty, None, Nil, Nil, Nil)
}
