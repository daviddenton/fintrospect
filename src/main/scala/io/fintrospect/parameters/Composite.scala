package io.fintrospect.parameters

/**
  * A composite is an outbound object which can be deserialised from many parameters in an inbound object, for example a
  * number of query parameters in a Request.
  */
object Composite {
  def apply[From, O](fn: From => Either[Seq[InvalidParameter], O]) = new Mandatory[O, From] {
    override protected def extract(from: From): Extraction[O] = fn(from).fold(ip => ExtractionFailed[O](ip), Extracted(_))
  }
}


