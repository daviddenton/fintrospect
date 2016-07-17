package io.fintrospect.parameters

import com.twitter.finagle.http.Message
import io.fintrospect.ContentTypes.APPLICATION_FORM_URLENCODED
import io.fintrospect.util.{Extracted, Extraction, ExtractionError, ExtractionFailed}

import scala.util.{Failure, Success, Try}


/**
  * Web-forms are a wrapper for standard forms, which holds both a collection of received fields and a set of invalid fields.
  * This form is to be used for web forms (where feedback is desirable and the user can be redirected back to the form page).
  * As such, extracting an invalid webform from a request will not fail unless the body encoding itself is invalid.
  */
class WebFormBody(form: FormBody, messages: Map[String, String])
  extends Body[WebForm](
    new BodySpec[WebForm](None, APPLICATION_FORM_URLENCODED, s => WebFormBody.decodeForm(form, messages, FormBody.decodeForm(s)),
      f => FormBody.encodeForm(f.form)))
    with Mandatory[Message, WebForm] {

  override def iterator = form.iterator

  def -->(value: Form): Seq[Binding] = form --> value

  override def <--?(message: Message): Extraction[WebForm] =
    Try(spec.deserialize(message.contentString)) match {
      case Success(webForm) => Extracted(Some(webForm))
      case Failure(e) => ExtractionFailed(form.fields.filter(_.required).map(p => ExtractionError(p.name, "Could not parse")))
    }
}

protected object WebFormBody {
  def decodeForm(formBody: FormBody, messages: Map[String, String], rawForm: Form) =
    WebForm(rawForm, formBody.fields.flatMap {
      _ <--? rawForm match {
        case ExtractionFailed(e) => e.map(er => ExtractionError(er.name, messages.getOrElse(er.name, er.reason)))
        case _ => Nil
      }
    })
}
