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

abstract class MultiFormField[T](spec: ParameterSpec[T])
  extends MultiParameter(spec, FormFieldExtractAndRebind) with FormField[Seq[T]] {
}

abstract class SingleFile(val name: String, val description: Option[String])
  extends Parameter with Bindable[MultiPartFile, FormFileBinding] with FormField[MultiPartFile] {
  override val paramType = FileParamType

  override def -->(value: MultiPartFile): Iterable[FormFileBinding] = Seq(new FormFileBinding(this, value))
}

abstract class MultiFile(val name: String, val description: Option[String])
  extends Parameter with Bindable[Seq[MultiPartFile], FormFileBinding] with FormField[Seq[MultiPartFile]] {
  override val paramType = FileParamType

  override def -->(value: Seq[MultiPartFile]): Iterable[FormFileBinding] = value.map(new FormFileBinding(this, _))
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

  val required = new Parameters[FormField, Mandatory] with MultiParameters[MultiFormField, MandatorySeq] {
    override def apply[T](spec: ParameterSpec[T]) = new SingleParameter(spec, FormFieldExtractAndRebind) with FormField[T] with Mandatory[T]

    def file(name: String, description: String = null) =
      new SingleFile(name, Option(description)) with MandatoryFile {
        override def <--?(form: Form): Extraction[MultiPartFile] = form.files.get(name)
          .map(files => Extracted(files.headOption)).getOrElse(ExtractionFailed(Missing(this)))
      }

    override def * = multi

    override val multi = new Parameters[MultiFormField, MandatorySeq] {
      override def apply[T](spec: ParameterSpec[T]) = new MultiFormField(spec) with MandatorySeq[T]

      def file(name: String, description: String = null) =
        new MultiFile(name, Option(description)) with MandatoryFileSeq {
          override def <--?(form: Form): Extraction[Seq[MultiPartFile]] = form.files.get(name)
            .map(files => Extracted(Option(files.toSeq))).getOrElse(ExtractionFailed(Missing(this)))
        }
    }
  }

  val optional = new Parameters[FormField, Optional] with MultiParameters[MultiFormField, OptionalSeq] {
    override def apply[T](spec: ParameterSpec[T]) = new SingleParameter(spec, FormFieldExtractAndRebind) with FormField[T] with Optional[T]

    def file(name: String, description: String = null) =
      new SingleFile(name, Option(description)) with OptionalFile {
        override def <--?(form: Form): Extraction[MultiPartFile] = Extracted(form.files.get(name).flatMap(_.headOption))
      }

    override def * = multi
    override val multi = new Parameters[MultiFormField, OptionalSeq] {
      override def apply[T](spec: ParameterSpec[T]) = new MultiFormField(spec) with OptionalSeq[T]

      def file(inName: String, inDescription: String = null) =
        new MultiFile(inName, Option(inDescription)) with OptionalFileSeq {
          override def <--?(form: Form): Extraction[Seq[MultiPartFile]] = Extracted(form.files.get(inName).map(_.toSeq))
        }
    }
  }
}
