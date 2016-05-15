package io.fintrospect.parameters

import io.fintrospect.parameters.InvalidParameter.{Invalid, Missing}

import scala.util.{Failure, Success, Try}

object Extractor {

  def extract[T](parameter: Parameter with Deserialisable[T], fromInput: Option[Seq[String]]): Extraction[T] = {
    fromInput.map {
      v =>
        Try(parameter.deserialize(v)) match {
          case Success(d) => Extracted(d)
          case Failure(_) => ExtractionFailed(Invalid(parameter))
        }
    }.getOrElse(if (parameter.required) ExtractionFailed(Missing(parameter)) else NotProvided())
  }

}
