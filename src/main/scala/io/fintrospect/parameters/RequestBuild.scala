package io.fintrospect.parameters

import org.jboss.netty.handler.codec.http._

case class RequestBuild(private val uriParts: Seq[String] = Seq(),
                        private val queries: Map[String, String] = Map(),
                        private val fn: HttpRequest => HttpRequest = identity) {
  def build(method: HttpMethod): HttpRequest = {
    val uri = queries.foldLeft(new QueryStringEncoder(uriParts.mkString("/"))) {
      (memo, q) =>
        memo.addParam(q._1, q._2)
        memo
    }.toString

    fn(new DefaultHttpRequest(HttpVersion.HTTP_1_1, method, uri))
  }

  def bind(binding: Binding): RequestBuild = binding match {
    case PathBinding(param, next) => copy(uriParts = uriParts :+ next)
    case QueryBinding(param, next) => copy(queries = queries + next)
    case RequestBinding(param, next) => copy(fn = fn.andThen(next))
  }
}
