package io.fintrospect.parameters

import scala.language.higherKinds

trait MultiParameters[P[_], R[_]] {
  val multi: Parameters[P, R]
}
