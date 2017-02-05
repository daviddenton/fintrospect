package io.fintrospect.parameters

import io.fintrospect.util.ExtractionError.Missing
import io.fintrospect.util.{Extracted, Extraction, ExtractionFailed}

trait FormField[T]
  extends BodyParameter {
  override val example = None
  override val where = "form"
}

private object FormFieldExtractAndRebind extends ParameterExtractAndBind[Form, String, FormFieldBinding] {
  def newBinding(parameter: Parameter, value: String) = new FormFieldBinding(parameter, value)

  def valuesFrom(parameter: Parameter, form: Form): Option[Seq[String]] = form.fields.get(parameter.name).map(_.toSeq)
}

private object FormFileExtractAndRebind extends ParameterExtractAndBind[Form, MultiPartFile, FormFileBinding] {
  def newBinding(parameter: Parameter, value: MultiPartFile) = new FormFileBinding(parameter, value)

  def valuesFrom(parameter: Parameter, form: Form): Option[Seq[MultiPartFile]] = form.files.get(parameter.name).map(_.toSeq)
}

abstract class ExtractableFormFile[Bind, Out](val name: String, val description: String,
                                              bindFn: Bind => Seq[MultiPartFile],
                                              tToOut: Seq[MultiPartFile] => Out,
                                              onMissing: Parameter => Extraction[Out])
  extends Parameter with Bindable[Bind, FormFileBinding] with FormField[Bind] {

  override def iterator: Iterator[Parameter] = Seq(this).iterator

  override val paramType = FileParamType

  def <--?(form: Form): Extraction[Out] = FormFileExtractAndRebind.valuesFrom(this, form).map(xs => Extracted(tToOut(xs))).getOrElse(onMissing(this))

  def -->(value: Bind): Seq[FormFileBinding] = bindFn(value).map(new FormFileBinding(this, _))
}

abstract class SingleMandatoryFormFile(name: String, description: String = null) extends
  ExtractableFormFile[MultiPartFile, MultiPartFile](name, description, Seq(_), _.head, p => ExtractionFailed(Missing(p))) {
}

abstract class SingleOptionalFormFile(name: String, description: String = null) extends
  ExtractableFormFile[MultiPartFile, Option[MultiPartFile]](name, description, Seq(_), _.headOption, p => Extracted(None)) {
}

abstract class MultiMandatoryFormFile(name: String, description: String = null) extends
  ExtractableFormFile[Seq[MultiPartFile], Seq[MultiPartFile]](name, description, identity, identity, p => ExtractionFailed(Missing(p))) {
}

abstract class MultiOptionalFormFile(name: String, description: String = null) extends
  ExtractableFormFile[Seq[MultiPartFile], Option[Seq[MultiPartFile]]](name, description, identity, Some(_), p => Extracted(None)) {
}

object FormField {

  type Mandatory[T] = MandatoryParameter[Form, T, FormFieldBinding]

  type MandatorySeq[T] = MandatoryParameter[Form, Seq[T], FormFieldBinding]

  type Optional[T] = OptionalParameter[Form, T, FormFieldBinding]

  type OptionalSeq[T] = OptionalParameter[Form, Seq[T], FormFieldBinding]

  type MandatoryFile = MandatoryParameter[Form, MultiPartFile, FormFileBinding]

  type MandatoryFileSeq = MandatoryParameter[Form, Seq[MultiPartFile], FormFileBinding]

  type OptionalFile = OptionalParameter[Form, MultiPartFile, FormFileBinding]

  type OptionalFileSeq = OptionalParameter[Form, Seq[MultiPartFile], FormFileBinding]

  type FSeq[T] = FormField[Seq[T]]

  trait WithFile[T] {
    def file(name: String, description: String = null): T
  }

  val required = new Parameters[FormField, Mandatory]
    with WithFile[MandatoryFile]
    with MultiParameters[FSeq, MandatorySeq] {
    override def apply[T](spec: ParameterSpec[T], name: String, description: String = null) =
      new SingleMandatoryParameter(name, description, spec, FormFieldExtractAndRebind) with FormField[T] with Mandatory[T]

    def file(name: String, description: String = null) = new SingleMandatoryFormFile(name, description) with MandatoryFile

    override def * = multi

    override val multi = new Parameters[FSeq, MandatorySeq]
      with WithFile[MultiMandatoryFormFile with MandatoryFileSeq] {

      override def apply[T](spec: ParameterSpec[T], name: String, description: String = null) =
        new MultiMandatoryParameter(name, description, spec, FormFieldExtractAndRebind) with FSeq[T] with MandatorySeq[T]

      def file(name: String, description: String = null) = new MultiMandatoryFormFile(name, description) with MandatoryFileSeq
    }
  }

  val optional = new Parameters[FormField, Optional]
    with WithFile[OptionalFile]
    with MultiParameters[FSeq, OptionalSeq] {

    override def apply[T](spec: ParameterSpec[T], name: String, description: String = null) =
      new SingleOptionalParameter(name, description, spec, FormFieldExtractAndRebind) with FormField[T] with Optional[T]

    def file(name: String, description: String = null) = new SingleOptionalFormFile(name, description) with OptionalFile

    override def * = multi

    override val multi = new Parameters[FSeq, OptionalSeq]
      with WithFile[MultiOptionalFormFile with OptionalFileSeq] {
      override def apply[T](spec: ParameterSpec[T], name: String, description: String = null) =
        new MultiOptionalParameter(name, description, spec, FormFieldExtractAndRebind) with FSeq[T] with OptionalSeq[T]

      def file(inName: String, description: String = null) = new MultiOptionalFormFile(inName, description) with OptionalFileSeq
    }
  }
}
