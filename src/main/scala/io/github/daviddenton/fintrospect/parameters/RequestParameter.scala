package io.github.daviddenton.fintrospect.parameters

import com.twitter.finagle.http.Request

import scala.reflect.ClassTag

abstract class RequestParameter[T](name: String, description: Option[String], location: Location, parse: (String => Option[T]))(implicit ct: ClassTag[T])
  extends Parameter[T, Request](name, description, location.toString)(ct) {
}
