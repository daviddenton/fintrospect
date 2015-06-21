package io.fintrospect.clients

import com.twitter.finagle.Service
import io.fintrospect.parameters.{Path, PathParameter}
import org.jboss.netty.handler.codec.http.{HttpMethod, HttpRequest, HttpResponse}

object ClientPath {
  def apply(client: ClientRoute, method: HttpMethod): ClientPath = ClientPath(client, method, Nil)
}

case class ClientPath private(client: ClientRoute, method: HttpMethod, pathParametersInReverse: Seq[PathParameter[_]]) {
  def /(part: String): ClientPath = /(Path.fixed(part))

  def /(pp: PathParameter[_]): ClientPath = copy(pathParametersInReverse = pp +: pathParametersInReverse)

  def bindTo(service: Service[HttpRequest, HttpResponse]) = new Client(method, client.headerParams, client.queryParams, pathParametersInReverse.reverse, service)
}

