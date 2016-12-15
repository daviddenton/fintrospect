package io.fintrospect.parameters

import com.twitter.finagle.http.{Request, RequestProxy}
import io.fintrospect.util.Extraction

case class ExtractedBodyRequest(override val request: Request, body: Extraction[_]) extends RequestProxy {
  def to[T]: Extraction[T] = body.asInstanceOf[Extraction[T]]
}
