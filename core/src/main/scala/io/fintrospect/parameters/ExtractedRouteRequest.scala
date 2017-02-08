package io.fintrospect.parameters

import com.twitter.finagle.http.{Request, RequestProxy}
import io.fintrospect.util.Extraction

case class ExtractedRouteRequest(override val request: Request, contents: Map[Any, Extraction[Any]]) extends RequestProxy {
  def get[T, Wrapper](p: Any, fallback: Wrapper => Extraction[T]): Extraction[T] =
    contents.getOrElse(p, fallback(request.asInstanceOf[Wrapper])).asInstanceOf[Extraction[T]]
}
