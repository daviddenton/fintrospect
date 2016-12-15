package io.fintrospect.parameters

import com.twitter.finagle.http.{Request, RequestProxy}
import io.fintrospect.util.ExtractionError.Missing
import io.fintrospect.util.{Extraction, ExtractionFailed}

case class ExtractedRouteRequest(override val request: Request, contents: Map[Any, Extraction[Any]]) extends RequestProxy {
  def get[T](p: Parameter): Extraction[T] = contents.getOrElse(p, ExtractionFailed(Missing(p))).asInstanceOf[Extraction[T]]

  def get[T](b: Body[T]): Extraction[T] = contents.getOrElse(b, b.map(Missing)).asInstanceOf[Extraction[T]]
}
