package io.fintrospect.parameters

import com.twitter.finagle.http.Request

/**
  * A composite is an object which can be deserialised from many parameters in a Request, for example a
  * number of query parameters.
  */
trait Composite[T] extends Mandatory[T, Request]

object Composite {
  def apply[T](fn: Request => Either[Seq[InvalidParameter], T]) = new Composite[T] {
    override def extract(from: Request): Extraction[T] = {
      fn(from).fold(ip => ExtractionFailed[T](ip), Extracted(_))
    }
  }
}


