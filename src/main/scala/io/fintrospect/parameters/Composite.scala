package io.fintrospect.parameters

/**
  * A composite is an object which can be deserialised from many parameters in a Request, for example a
  * number of query parameters.
  */
object Composite {
  def apply[From, O](fn: From => Either[Seq[InvalidParameter], O]) = new Mandatory[O, From] {
    override protected def extract(from: From): Extraction[O] = fn(from).fold(ip => ExtractionFailed[O](ip), Extracted(_))
  }
}


