package io.fintrospect.parameters

import scala.language.higherKinds

/**
  * Support for parameters which can have more than one value (e.g query parameters or forms)
  */
trait MultiParameters[P[_], R[_]] {

  val multi: Parameters[P, R]

  def * = multi
}
