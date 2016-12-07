package io.fintrospect.parameters

sealed trait StringValidation extends (String => String)

object StringValidation {
  val EmptyIsValid = new StringValidation {
    override def apply(in: String): String = in.toString
  }
  val EmptyIsInvalid = new StringValidation {
    override def apply(in: String): String = if (in.isEmpty) throw new IllegalArgumentException("Cannot be empty") else in.toString
  }
}

sealed trait FileValidation extends (MultiPartFile => MultiPartFile)

object FileValidation {
  val EmptyIsValid = new FileValidation {
    override def apply(in: MultiPartFile): MultiPartFile = in
  }

  val EmptyIsInvalid = new FileValidation {
    override def apply(in: MultiPartFile): MultiPartFile = in match {
      case InMemoryMultiPartFile(content, _, _) => if (content.isEmpty) throw new IllegalArgumentException("Cannot be empty") else in
      case _ => in
    }
  }
}
