package io.fintrospect.parameters

import java.net.{URLDecoder, URLEncoder}

import com.twitter.finagle.http._
import io.fintrospect.ContentTypes.APPLICATION_FORM_URLENCODED
import io.fintrospect.util.{Extraction, ExtractionError, ExtractionFailed, Extractor}
import org.jboss.netty.handler.codec.http.HttpHeaders.Names

import scala.util.{Failure, Success, Try}

class UrlEncodedFormBody(fields: Seq[FormField[_] with Extractor[Form, _]],
                         validator: FormValidator, extractor: FormFieldExtractor)
  extends Body[Form] {

  override val contentType = APPLICATION_FORM_URLENCODED

  override def iterator = fields.iterator

  private def decodeFields(content: String): Map[String, Set[String]] = {
    content
      .split("&")
      .filter(_.contains("="))
      .map(nvp => {
        val parts = nvp.split("=")
        (URLDecoder.decode(parts(0), "UTF-8"), if (parts.length > 1) URLDecoder.decode(parts(1), "UTF-8") else "")
      })
      .groupBy(_._1)
      .mapValues(_.map(_._2))
      .mapValues(_.toSet)
  }

  private def encode(form: Form): String = form.fields.flatMap {
    case (name, values) => values.map(value => URLEncoder.encode(name, "UTF-8") + "=" + URLEncoder.encode(value, "UTF-8"))
  }.mkString("&")


  def -->(value: Form): Seq[RequestBinding] =
    Seq(new RequestBinding(null, req => {
      val contentString = encode(value)
      req.headerMap.add(Names.CONTENT_TYPE, contentType.value)
      req.headerMap.add(Names.CONTENT_LENGTH, contentString.length.toString)
      req.contentString = contentString
      req
    })) ++ fields.map(f => new FormFieldBinding(f, ""))

  override def <--?(message: Message): Extraction[Form] =
    Try(validator(fields, new Form(decodeFields(message.contentString), Map.empty, Nil))) match {
      case Success(form) => extractor(fields, form)
      case Failure(e) => ExtractionFailed(fields.filter(_.required).map(param => ExtractionError(param, "Could not parse")))
    }
}
