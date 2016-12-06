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

abstract class SingleFile(inName: String, inDescription: String = null) extends Parameter with Bindable[MultiPartFile, FormFileBinding] with FormField[MultiPartFile] {
  override val name = inName
  override val description = Option(inDescription)
  override val paramType = FileParamType

  override def -->(value: MultiPartFile): Iterable[FormFileBinding] = Seq(new FormFileBinding(this, value))
}

abstract class MultiFile(inName: String, inDescription: String = null) extends Parameter with Bindable[Seq[MultiPartFile], FormFileBinding] with FormField[Seq[MultiPartFile]] {
  override val name = inName
  override val description = Option(inDescription)
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

    def file(inName: String, inDescription: String = null) = new SingleFile(inName, inDescription) with MandatoryFile {
      override def <--?(form: Form): Extraction[MultiPartFile] =
        form.files.get(inName) match {
          case Some(files) => Extracted(files.headOption)
          case None => ExtractionFailed(Missing(this))
        }
    }

    override val multi = new Parameters[MultiFormField, MandatorySeq] {
      override def apply[T](spec: ParameterSpec[T]) = new MultiFormField(spec) with MandatorySeq[T]

      def file(inName: String, inDescription: String = null) =
        new MultiFile(inName, inDescription) with MandatoryFileSeq {
          override def <--?(form: Form): Extraction[Seq[MultiPartFile]] = form.files.get(inName) match {
            case Some(files) => Extracted(Option(files.toSeq))
            case None => ExtractionFailed(Missing(this))
          }
        }
    }
  }

  val optional = new Parameters[FormField, Optional] with MultiParameters[MultiFormField, OptionalSeq] {
    override def apply[T](spec: ParameterSpec[T]) = new SingleParameter(spec, FormFieldExtractAndRebind) with FormField[T] with Optional[T]

    def file(inName: String, inDescription: String = null) = new SingleFile(inName, inDescription) with OptionalFile {
      override def <--?(form: Form): Extraction[MultiPartFile] = Extracted(form.files.get(inName).flatMap(_.headOption))
    }

    override val multi = new Parameters[MultiFormField, OptionalSeq] {
      override def apply[T](spec: ParameterSpec[T]) = new MultiFormField(spec) with OptionalSeq[T]

      def file(inName: String, inDescription: String = null) =
        new MultiFile(inName, inDescription) with OptionalFileSeq {
          override def <--?(form: Form): Extraction[Seq[MultiPartFile]] = form.files.get(inName) match {
            case Some(files) => Extracted(Option(files.toSeq))
            case None => Extracted(None)
          }
        }

    }
  }
}
