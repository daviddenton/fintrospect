package io.fintrospect.parameters

import com.twitter.finagle.http.Request

import scala.collection.mutable

/**
  * A composite allows for a single object to be extracted from many request parameters. Validation is
  * applied to all of the parts before being passed to a user implemented service.
  * Extend this to provide custom extraction for composite objects.
  */
trait Composite[T] extends Mandatory[Request, T] with HasParameters
  with Rebindable[Request, T, Binding] {

  private val params = mutable.ListBuffer[Parameter]()

  /**
    * Add a parameter to the list of parameters that make up this composite.
    */
  protected[Composite] def add[A <: Parameter](a: A): A = {
    params += a
    a
  }

  override def iterator: Iterator[Parameter] = params.iterator

  override def <->(from: Request): Iterable[Binding] = this --> (this <-- from)

  /**
    * Create a the bindings to bind this object to a request. Implement this method only if you care about sending
    * this object to another service.
    */
  override def -->(value: T): Iterable[Binding] = throw new UnsupportedOperationException(
    "Implement this method only if you care about sending this object to another service."
  )
}


