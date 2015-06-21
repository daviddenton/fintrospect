package io.fintrospect.clients

import io.fintrospect.parameters.{Body, HeaderParameter, QueryParameter}
import org.jboss.netty.handler.codec.http.HttpMethod

object ClientRoute {
  def apply(): ClientRoute = ClientRoute(Nil, Nil, None)
}

case class ClientRoute private(queryParams: Seq[QueryParameter[_]],
                               headerParams: Seq[HeaderParameter[_]],
                               body: Option[Body[_]]) {
  def taking(rp: HeaderParameter[_]) = copy(headerParams = rp +: headerParams)
  def taking(rp: QueryParameter[_]) = copy(queryParams = rp +: queryParams)
  def body(bp: Body[_]) = copy(body = Option(bp))

  def at(method: HttpMethod) = ClientPath(this, method)
}
