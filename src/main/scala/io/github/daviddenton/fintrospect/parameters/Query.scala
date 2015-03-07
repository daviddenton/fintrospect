package io.github.daviddenton.fintrospect.parameters

import com.twitter.finagle.http.Request

import scala.reflect.ClassTag

object Query extends Parameters[RequestParameter]() {
  private val location = new Location {
    override def toString = "query"

    override def from(name: String, request: Request): Option[String] = request.params.get(name)
  }

  protected def create[T](name: String, required: Requirement, parse: (String => Option[T]))(implicit ct: ClassTag[T]) = new RequestParameter[T](name, location, required, parse)
}
