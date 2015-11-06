package io.fintrospect

/**
 * Implement this trait if there is a requirement to have more than one route in a single class.
 */
trait ServerRoutes extends Iterable[ServerRoute] {
  private val definedRoutes = scala.collection.mutable.MutableList[ServerRoute]()

  protected def add(route: ServerRoute) = definedRoutes += route

  override def iterator: Iterator[ServerRoute] = definedRoutes.iterator
}
