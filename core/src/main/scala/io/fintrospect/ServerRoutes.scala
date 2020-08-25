package io.fintrospect

/**
  * Implement this trait if there is a requirement to have more than one route in a single class.
  */
trait ServerRoutes[RQ, RS] extends Iterable[ServerRoute[RQ, RS]] {
  private val definedRoutes = scala.collection.mutable.ArrayDeque[ServerRoute[RQ, RS]]()

  protected def add(route: ServerRoute[RQ, RS]) = definedRoutes += route

  override def iterator: Iterator[ServerRoute[RQ, RS]] = definedRoutes.iterator
}
