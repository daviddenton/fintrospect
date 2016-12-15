package io.fintrospect.parameters

import com.twitter.finagle.http.{Request, RequestProxy}
import io.fintrospect.util.{Extraction, ExtractionError, ExtractionFailed}

case class ExtractedRouteRequest(override val request: Request, contents: Map[Any, Extraction[Any]]) extends RequestProxy {
  def get[T](p: Parameter): Extraction[T] = contents.getOrElse(p, ExtractionFailed(ExtractionError.Missing(p))).asInstanceOf[Extraction[T]]
  def get[T](p: Body[T]): Extraction[T] = contents.getOrElse(p, p.map(ExtractionError.Missing)).asInstanceOf[Extraction[T]]
}
