package io.fintrospect.parameters

import com.twitter.finagle.http.exp.Multipart.FileUpload
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

private object FormFileExtractAndRebind extends ParameterExtractAndBind[Form, FileUpload, FormFileBinding] {
  def newBinding(parameter: Parameter, value: FileUpload) = new FormFileBinding(parameter, value)

  def valuesFrom(parameter: Parameter, form: Form): Option[Seq[FileUpload]] = form.files.get(parameter.name).map(_.toSeq)
}

abstract class MultiFormField[T](spec: ParameterSpec[T])
  extends MultiParameter(spec, FormFieldExtractAndRebind) with FormField[Seq[T]] {
}

abstract class SingleFile(inName: String, inDescription: String = null) extends Parameter with Bindable[FileUpload, FormFileBinding] with FormField[FileUpload] {
  override val name = inName
  override val description = Option(inDescription)
  override val paramType = FileParamType

  override def -->(value: FileUpload): Iterable[FormFileBinding] = Seq(new FormFileBinding(this, value))
}

abstract class MultiFile(inName: String, inDescription: String = null) extends Parameter with Bindable[Seq[FileUpload], FormFileBinding] with FormField[Seq[FileUpload]] {
  override val name = inName
  override val description = Option(inDescription)
  override val paramType = FileParamType

  override def -->(value: Seq[FileUpload]): Iterable[FormFileBinding] = value.map(new FormFileBinding(this, _))
}

object FormField {

  type Mandatory[T] = MandatoryParameter[Form, T, FormFieldBinding]

  type MandatorySeq[T] = MandatoryParameter[Form, Seq[T], FormFieldBinding]

  type Optional[T] = OptionalParameter[Form, T, FormFieldBinding]

  type OptionalSeq[T] = OptionalParameter[Form, Seq[T], FormFieldBinding]

  type MandatoryFile = MandatoryParameter[Form, FileUpload, FormFileBinding]

  type MandatoryFileSeq = MandatoryParameter[Form, Seq[FileUpload], FormFileBinding]

  type OptionalFile = OptionalParameter[Form, FileUpload, FormFileBinding]

  type OptionalFileSeq = OptionalParameter[Form, Seq[FileUpload], FormFileBinding]

  val required = new Parameters[FormField, Mandatory] with MultiParameters[MultiFormField, MandatorySeq] {
    override def apply[T](spec: ParameterSpec[T]) = new SingleParameter(spec, FormFieldExtractAndRebind) with FormField[T] with Mandatory[T]

    override val multi = new Parameters[MultiFormField, MandatorySeq] {
      override def apply[T](spec: ParameterSpec[T]) = new MultiFormField(spec) with MandatorySeq[T]

      def file(inName: String, inDescription: String = null) =
        new MultiFile(inName, inDescription) with MandatoryFileSeq {
          override def <--?(form: Form): Extraction[Seq[FileUpload]] = form.files.get(inName) match {
            case Some(s) => Extracted(Option(s.toSeq))
            case None => ExtractionFailed(Missing(this))
          }
        }
    }

    def file(inName: String, inDescription: String = null) =
      new SingleFile(inName, inDescription) with MandatoryFile {
        override def <--?(form: Form): Extraction[FileUpload] = form.files.get(inName) match {
          case Some(s) => Extracted(s.headOption)
          case None => ExtractionFailed(Missing(this))
        }
      }
  }

  val optional = new Parameters[FormField, Optional] with MultiParameters[MultiFormField, OptionalSeq] {
    override def apply[T](spec: ParameterSpec[T]) = new SingleParameter(spec, FormFieldExtractAndRebind) with FormField[T] with Optional[T]

    override val multi = new Parameters[MultiFormField, OptionalSeq] {
      override def apply[T](spec: ParameterSpec[T]) = new MultiFormField(spec) with OptionalSeq[T]

      def file(inName: String, inDescription: String = null) =
        new MultiFile(inName, inDescription) with OptionalFileSeq {
          override def <--?(form: Form): Extraction[Seq[FileUpload]] = form.files.get(inName) match {
            case Some(s) => Extracted(Option(s.toSeq))
            case None => Extracted(None)
          }
        }

    }

    def file(inName: String, inDescription: String = null) =
      new SingleFile(inName, inDescription) with OptionalFile {
        override def <--?(form: Form): Extraction[FileUpload] = Extracted(form.files.get(inName).flatMap(_.headOption))
      }
  }
}
