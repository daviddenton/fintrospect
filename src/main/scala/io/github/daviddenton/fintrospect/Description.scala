package io.github.daviddenton.fintrospect

import com.twitter.finagle.http.path.Path
import io.github.daviddenton.fintrospect.parameters.RequestParameter
import org.jboss.netty.handler.codec.http.{HttpMethod, HttpResponseStatus}

case class Description private(value: String, method: HttpMethod, params: List[RequestParameter[_]], responses: Map[HttpResponseStatus, String], complete: (Path => Path)) {
  def requiring(rp: RequestParameter[_]) = copy(params = rp :: params)
  def returning(codeAndDescription: (HttpResponseStatus, String)) = copy(responses = responses + codeAndDescription)
  def matches(method: HttpMethod, rootPath: Path, path: Path) = method == this.method && path == complete(rootPath)
}

object Description {
  def apply(value: String, method: HttpMethod, complete: (Path => Path)): Description = Description(value, method, Nil, Map.empty, complete)
}