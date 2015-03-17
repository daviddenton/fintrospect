package io.github.daviddenton.fintrospect.parameters

import com.twitter.finagle.http.Request

import scala.reflect.ClassTag

object Header {

  private val location = new Location {
    override def toString = "header"

    override def from(name: String, request: Request): Option[String] = Option(request.headers().get(name))
  }

  val required = new Parameters[RequiredRequestParameter] {
    protected def create[T](name: String, description: Option[String], parse: (String => Option[T]))(implicit ct: ClassTag[T]) = new RequiredRequestParameter[T](name, description, location, parse)
  }

  val optional: Parameters[OptionalRequestParameter] = new Parameters[OptionalRequestParameter] {
    protected def create[T](name: String, description: Option[String], parse: (String => Option[T]))(implicit ct: ClassTag[T]) = new OptionalRequestParameter[T](name, description, location, parse)
  }
}
