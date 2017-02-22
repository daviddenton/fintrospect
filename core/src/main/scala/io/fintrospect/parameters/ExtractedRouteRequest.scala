package io.fintrospect.parameters

import com.twitter.finagle.http.{Request, RequestProxy}
import io.fintrospect.util.Extraction

case class ExtractedRouteRequest(override val request: Request, private val contents: Map[Any, Extraction[Any]]) extends RequestProxy {
  private val contentsByName = contents.map(e => e._1.toString -> e._2)
  def get[T, Wrapper](p: Any, fallback: Wrapper => Extraction[T]): Extraction[T] = contentsByName.getOrElse(p.toString, fallback(request.asInstanceOf[Wrapper])).asInstanceOf[Extraction[T]]
}
