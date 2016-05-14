package io.fintrospect.parameters

import com.twitter.finagle.http.Request

/**
  * A composite is an object which can be deserialised from many parameters in a Request, for example a
  * number of query parameters.
  */
abstract class Composite[T] extends Retrieval[T, Request]
with Validatable[T, Request] {
  override def <--(message: Request): T = validate(message).asTry.get.get
}

object Composite {
  def apply[T](fn: Request => Extraction[T]) = new Composite[T] {
    override def <--?(from: Request): Extraction[T] = fn(from)
  }
}


