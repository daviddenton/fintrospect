package io.github.daviddenton.fintrospect

import com.twitter.finagle.http.path.Path
import org.jboss.netty.handler.codec.http.HttpMethod

object FintrospectTypes {
  type Request = com.twitter.finagle.http.Request
  type Response = com.twitter.finagle.http.Response
  type Service = com.twitter.finagle.Service[Request, Response]
  type Filter = com.twitter.finagle.Filter[Request, Response, Request, Response]
  type Binding = PartialFunction[(HttpMethod, Path), Service]
}
