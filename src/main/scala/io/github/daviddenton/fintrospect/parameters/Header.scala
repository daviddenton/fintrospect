package io.github.daviddenton.fintrospect.parameters

import com.twitter.finagle.http.Request

import scala.reflect.ClassTag

object Header extends Parameters[RequestParameter]() {

  private val location = new Location {
    override def toString = "header"

    override def from(name: String, request: Request): Option[String] = Option(request.headers().get(name))
  }

  protected def create[T](name: String, description: Option[String], parse: (String => Option[T]))(implicit ct: ClassTag[T]) = new RequestParameter[T](name, description, location, parse)
}
