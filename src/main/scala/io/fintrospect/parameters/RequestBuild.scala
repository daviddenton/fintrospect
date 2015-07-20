package io.fintrospect.parameters

import com.twitter.finagle.http.Request
import org.jboss.netty.handler.codec.http._

case class RequestBuild(uriParts: Seq[String] = Seq(),
                        queries: Map[String, Seq[String]] = Map(),
                        fn: HttpRequest => HttpRequest = identity) {
  def build(method: HttpMethod): HttpRequest = {
    val uri = queries.foldLeft(new QueryStringEncoder(uriParts.mkString("/"))) {
      (memo, q) =>
        q._2.foreach(v => memo.addParam(q._1, v))
        memo
    }.toString

    fn(Request(method, s"/$uri"))
  }
}
