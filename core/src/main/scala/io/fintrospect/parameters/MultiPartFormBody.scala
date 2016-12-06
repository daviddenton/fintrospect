package io.fintrospect.parameters

import com.twitter.finagle.http._
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
      val files = value.files.flatMap(f => f._2.map(_.toFormElement(f._1))).toSeq

      val next = RequestBuilder()
        .url("http://notreallyaserver")
        .addHeaders(Map(req.headerMap.toSeq: _*))
        .add(fields ++ files)
        .buildFormPost(multipart = true)
      next.uri = req.uri
      next
    }))

  override def <--?(message: Message): Extraction[Form] = message.asInstanceOf[Request].multipart
    .map(m => {
      val multipart = message.asInstanceOf[Request].multipart.get
      Try(validator(formContents, Form(multipart.attributes.mapValues(_.toSet), multipart.files.mapValues(_.map(MultiPartFile(_)).toSet)))) match {
        case Success(form) => extractor(formContents, form)
        case Failure(e) => ExtractionFailed(formContents.filter(_.required).map(param => ExtractionError(param, "Could not parse")))
      }
    }).getOrElse(ExtractionFailed(formContents.filter(_.required).map(param => ExtractionError(param, "Could not parse"))))
}