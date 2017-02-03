package io.fintrospect.parameters

import com.twitter.finagle.http.Request
import io.fintrospect.RouteSpec.QueryOrHeader

import scala.collection.mutable

/**
  * A request composite allows for a single object to be extracted from many request parameters. Extend this to
  * provide custom extraction for composite objects.
  */
trait RequestComposite[T] extends Mandatory[Request, T] with HasParameters with Rebindable[Request, T, RequestBinding] {

  private val params = mutable.ListBuffer[Parameter]()

  /**
    * Add a parameter to the list of parameters that make up this
    */
  protected[RequestComposite] def add[A <: Parameter](a: A): A = {
    params += a
    a
  }

  override def iterator: Iterator[Parameter] = params.iterator

  /**
    * Bind the value to this parameter
    *
    * @return the binding
    */
  override def -->(value: T): Iterable[RequestBinding] = ???

  override def <->(from: Request): Iterable[RequestBinding] = ???
}


