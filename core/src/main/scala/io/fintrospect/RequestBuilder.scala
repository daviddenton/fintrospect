package io.fintrospect

import com.twitter.finagle.http.{Method, Request}
import io.fintrospect.util.Builder
import org.jboss.netty.handler.codec.http.QueryStringEncoder

case class RequestBuilder(method: Method,
                        uriParts: Seq[String] = Nil,
                        queries: Map[String, Seq[String]] = Map(),
                        fn: Request => Request = identity) extends Builder[Request] {
  def build(): Request = {
    val baseUri = uriParts.mkString("/")
    val uri = queries.foldLeft(new QueryStringEncoder(if (baseUri.isEmpty) "/" else baseUri)) {
      (memo, q) =>
        q._2.foreach(v => memo.addParam(q._1, v))
        memo
    }.toString

    fn(Request(method, uri))
  }
}
