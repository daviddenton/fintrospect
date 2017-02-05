package io.fintrospect.parameters

import io.fintrospect.util.ExtractionError.Missing
import io.fintrospect.util.{Extracted, Extraction, ExtractionFailed}

object FormFile {
  type Mandatory = MandatoryParameter[Form, MultiPartFile, FormFileBinding]

  type MandatorySeq = MandatoryParameter[Form, Seq[MultiPartFile], FormFileBinding]

  type Optional = OptionalParameter[Form, MultiPartFile, FormFileBinding]

  type OptionalSeq = OptionalParameter[Form, Seq[MultiPartFile], FormFileBinding]
}

abstract class ExtractableFormFile[Bind, Out](val name: String, val description: String,
                                              bindFn: Bind => Seq[MultiPartFile],
                                              tToOut: Seq[MultiPartFile] => Out,
                                              onMissing: Parameter => Extraction[Out])
  extends Parameter with Bindable[Bind, FormFileBinding] with FormField[Bind] {

  override def iterator: Iterator[Parameter] = Seq(this).iterator

  override val paramType = FileParamType

  def <--?(form: Form): Extraction[Out] = form.files.get(name)
    .map(_.toSeq)
    .map(xs => Extracted(tToOut(xs)))
    .getOrElse(onMissing(this))

  def -->(value: Bind): Seq[FormFileBinding] = bindFn(value).map(new FormFileBinding(this, _))
}

abstract class SingleMandatoryFormFile(name: String, description: String = null) extends
  ExtractableFormFile[MultiPartFile, MultiPartFile](name, description, Seq(_), _.head, p => ExtractionFailed(Missing(p))) {
}

abstract class SingleOptionalFormFile(name: String, description: String = null) extends
  ExtractableFormFile[MultiPartFile, Option[MultiPartFile]](name, description, Seq(_), _.headOption, _ => Extracted(None)) {
}

abstract class MultiMandatoryFormFile(name: String, description: String = null) extends
  ExtractableFormFile[Seq[MultiPartFile], Seq[MultiPartFile]](name, description, identity, identity, p => ExtractionFailed(Missing(p))) {
}

abstract class MultiOptionalFormFile(name: String, description: String = null) extends
  ExtractableFormFile[Seq[MultiPartFile], Option[Seq[MultiPartFile]]](name, description, identity, Some(_), _ => Extracted(None)) {
}
