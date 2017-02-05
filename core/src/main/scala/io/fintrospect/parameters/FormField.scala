package io.fintrospect.parameters

import io.fintrospect.parameters.FormFile.{MandatoryFile, MandatoryFileSeq, OptionalFile, OptionalFileSeq}

trait FormField[T]
  extends BodyParameter {
  override val example = None
  override val where = "form"
}

private object FormFieldExtractAndRebind extends ParameterExtractAndBind[Form, String, FormFieldBinding] {
  def newBinding(parameter: Parameter, value: String) = new FormFieldBinding(parameter, value)

  def valuesFrom(parameter: Parameter, form: Form): Option[Seq[String]] = form.fields.get(parameter.name).map(_.toSeq)
}

object FormField {

  type Mandatory[T] = MandatoryParameter[Form, T, FormFieldBinding]

  type MandatorySeq[T] = MandatoryParameter[Form, Seq[T], FormFieldBinding]

  type Optional[T] = OptionalParameter[Form, T, FormFieldBinding]

  type OptionalSeq[T] = OptionalParameter[Form, Seq[T], FormFieldBinding]

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
