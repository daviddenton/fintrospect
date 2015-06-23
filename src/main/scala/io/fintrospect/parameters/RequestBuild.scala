package io.fintrospect.parameters

import org.jboss.netty.handler.codec.http._

case class RequestBuild(private val uri: Seq[String] = Seq(),
                        private val queries: Map[String, String] = Map(),
                        private val fn: HttpRequest => HttpRequest = identity) {
  def build(method: HttpMethod): HttpRequest = {
    val string = uri.mkString("/")

    new QueryStringEncoder(uri.mkString("/"))

    fn(new DefaultHttpRequest(HttpVersion.HTTP_1_1, method, string))
  }

  def bind(binding: Binding): RequestBuild = binding match {
    case PathBinding(param, next) => copy(uri = uri :+ next)
    case QueryBinding(next) => copy(queries = queries + next)
    case RequestBinding(next) => copy(fn = fn.andThen(next))
  }
}
