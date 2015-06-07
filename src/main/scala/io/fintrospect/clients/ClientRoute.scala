package io.fintrospect.clients

import io.fintrospect.parameters.RequestParameter
import org.jboss.netty.handler.codec.http.HttpMethod

object ClientRoute {
  def apply(): ClientRoute = ClientRoute(Nil)
}

case class ClientRoute private(requestParameters: List[RequestParameter[_]]) {
  def taking(rp: RequestParameter[_]) = copy(requestParameters = rp :: requestParameters)

  def at(method: HttpMethod) = ClientPath(this, method)
}
