package io.fintrospect.parameters

import com.twitter.finagle.httpx.{Method, Request}
import org.jboss.netty.handler.codec.http._

case class RequestBuild(uriParts: Seq[String] = Nil,
                        queries: Map[String, Seq[String]] = Map(),
                        fn: Request => Request = identity) {
  def build(method: Method): Request = {
    val baseUri = uriParts.mkString("/")
    val uri = queries.foldLeft(new QueryStringEncoder(if(baseUri.isEmpty) "/" else baseUri)) {
      (memo, q) =>
        q._2.foreach(v => memo.addParam(q._1, v))
        memo
    }.toString

    fn(Request(method, uri))
  }
}
