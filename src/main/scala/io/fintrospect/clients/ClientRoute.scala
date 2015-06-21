package io.fintrospect.clients

import io.fintrospect.parameters.{Body, RequestParameter}
import org.jboss.netty.handler.codec.http.HttpMethod

object ClientRoute {
  def apply(): ClientRoute = ClientRoute(Nil, None)
}

case class ClientRoute private(requestParameters: Seq[RequestParameter[_]], body: Option[Body[_]]) {
  def taking(rp: RequestParameter[_]) = copy(requestParameters = rp +: requestParameters)
  def body(bp: Body[_]) = copy(body = Option(bp))

  def at(method: HttpMethod) = ClientPath(this, method)
}
