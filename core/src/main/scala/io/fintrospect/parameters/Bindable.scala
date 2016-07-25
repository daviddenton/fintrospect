package io.fintrospect.parameters


/**
  * Allows binding of a value to an entity (eg. query/header/field...)
  */
trait Bindable[-T, +B <: Binding] {

  /**
    * Bind the value to this parameter
    * User-friendly synonym for -->(), which is why the method is final.
    * @return the binding
    */
  final def of(value: T): Iterable[B] = -->(value)

  /**
    * Bind the value to this parameter
    * @return the binding
    */
  def -->(value: T): Iterable[B]
}

