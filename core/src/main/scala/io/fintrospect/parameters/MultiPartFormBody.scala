package io.fintrospect.parameters

import com.twitter.finagle.http._
import com.twitter.finagle.http.exp.Multipart
import io.fintrospect.ContentTypes.MULTIPART_FORM
import io.fintrospect.util.{Extraction, ExtractionError, ExtractionFailed, Extractor}

import scala.util.{Failure, Success, Try}

class MultiPartFormBody(formContents: Seq[FormField[_] with Extractor[Form, _]],
                        validator: FormValidator, extractor: FormFieldExtractor)
  extends Body[Form] {

  override val contentType = MULTIPART_FORM

  override def iterator = formContents.iterator

  def -->(value: Form): Seq[RequestBinding] =
    Seq(new RequestBinding(null, req => {
      val fields = value.fields.flatMap(f => f._2.map(g => SimpleElement(f._1, g))).toSeq
      val files = value.files.flatMap(f => f._2.map(_.toFileElement(f._1))).toSeq

      val next = RequestBuilder()
        .url("http://notreallyaserver")
        .addHeaders(Map(req.headerMap.toSeq: _*))
        .add(fields ++ files)
        .buildFormPost(multipart = true)
      next.uri = req.uri
      next
    }))

  // FIXME - horrid cast
  override def <--?(message: Message): Extraction[Form] = message.asInstanceOf[Request].multipart
    .map(multipart => {
      Try(validator(formContents, Form(multipart.attributes.mapValues(_.toSet), filterOutFilesWithNoFilename(multipart)))) match {
        case Success(form) => extractor(formContents, form)
        case Failure(e) => ExtractionFailed(formContents.filter(_.required).map(param => ExtractionError(param, "Could not parse")))
      }
    }).getOrElse(ExtractionFailed(formContents.filter(_.required).map(param => ExtractionError(param, "Could not parse"))))

  private def filterOutFilesWithNoFilename(multipart: Multipart) = {
    multipart.files
      .mapValues(_.filterNot(_.fileName.isEmpty)
        .map(MultiPartFile(_)).toSet)
      .filterNot(_._2.isEmpty)
  }
}