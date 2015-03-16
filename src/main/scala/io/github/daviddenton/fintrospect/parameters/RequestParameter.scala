package io.github.daviddenton.fintrospect.parameters

import com.twitter.finagle.http.Request

import scala.reflect.ClassTag

class RequestParameter[T](name: String, description: Option[String], location: Location, parse: (String => Option[T]))(implicit ct: ClassTag[T])
  extends Parameter[T, Request](name, description, location.toString)(ct) {
  def unapply(request: Request): Option[T] = location.from(name, request).flatMap(parse)
}
