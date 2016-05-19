package io.fintrospect.parameters

/**
  * A composite is an outbound object which can be deserialised from many parameters in an inbound object, for example a
  * number of query parameters in a Request.
  */
object Composite {
  def apply[From, O](fn: From => Extraction[O]) = new Mandatory[From, O] {
    override def <--?(from: From): Extraction[O] = fn(from)
  }
}




