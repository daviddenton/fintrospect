package io.fintrospect.parameters

import com.twitter.finagle.http._
import com.twitter.finagle.http.exp.Multipart.{FileUpload, InMemoryFileUpload, OnDiskFileUpload}
import com.twitter.io.Buf
import io.fintrospect.ContentTypes.MULTIPART_FORM
import io.fintrospect.util.{Extraction, ExtractionError, ExtractionFailed, Extractor}

import scala.util.{Failure, Success, Try}

class MultiPartFormBody(fields: Seq[FormField[_] with Extractor[Form, _]],
                        validator: FormValidator, extractor: FormFieldExtractor)
  extends Body[Form] {

  override val contentType = MULTIPART_FORM

  override def iterator = fields.iterator

  def -->(value: Form): Seq[RequestBinding] =
    Seq(new RequestBinding(null, req => {
      val fields = value.fields.flatMap(f => f._2.map(g => SimpleElement(f._1, g))).toSeq
      val files = value.files.flatMap(f => f._2.map(g => FileElement(f._1, g.content, g.contentType, g.filename))).toSeq

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
      Try(validator(fields, Form(multipart.attributes.mapValues(_.toSet), multipart.files.mapValues(_.map(toMultipartFile).toSet)))) match {
        case Success(form) => extractor(fields, form)
        case Failure(e) => ExtractionFailed(fields.filter(_.required).map(param => ExtractionError(param, "Could not parse")))
      }
    }).getOrElse(ExtractionFailed(fields.filter(_.required).map(param => ExtractionError(param, "Could not parse"))))

  private def toMultipartFile(f: FileUpload) = {
    f match {
      case InMemoryFileUpload(content, fileType, name, _) => MultiPartFile(content, Option(fileType), Option(name))
      // FIXME - OnDiskUploads!
      case OnDiskFileUpload(_, fileType, name, _) => MultiPartFile(Buf.Empty, Option(fileType), Option(name))
    }
  }
}
