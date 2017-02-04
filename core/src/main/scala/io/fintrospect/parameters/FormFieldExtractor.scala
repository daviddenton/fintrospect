package io.fintrospect.parameters

import io.fintrospect.util.{Extracted, Extraction, ExtractionFailed, Extractor}

sealed trait FormFieldExtractor {
  def apply(fields: Seq[Extractor[Form, _]], f: Form): Extraction[Form]
}

object WebFormFieldExtractor extends FormFieldExtractor {
  override def apply(fields: Seq[Extractor[Form, _]], t: Form): Extraction[Form] = Extracted(t)
}

object StrictFormFieldExtractor extends FormFieldExtractor {
  override def apply(fields: Seq[Extractor[Form, _]], form: Form): Extraction[Form] = Extraction.combine(fields.map(_.extract(form))) match {
    case failed@ExtractionFailed(_) => failed
    case _ => Extracted(form)
  }
}

