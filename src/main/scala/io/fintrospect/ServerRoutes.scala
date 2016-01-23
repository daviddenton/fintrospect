package io.fintrospect

/**
 * Implement this trait if there is a requirement to have more than one route in a single class.
 */
trait ServerRoutes[T] extends Iterable[ServerRoute[T]] {
  private val definedRoutes = scala.collection.mutable.MutableList[ServerRoute[T]]()

  protected def add(route: ServerRoute[T]) = definedRoutes += route

  override def iterator: Iterator[ServerRoute[T]] = definedRoutes.iterator
}
