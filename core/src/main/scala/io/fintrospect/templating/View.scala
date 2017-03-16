package io.fintrospect.templating

import com.twitter.finagle.http.Status
import com.twitter.util.Future

import scala.language.implicitConversions

/**
  * View model for use with templating system.
  */
trait View {
  val status: Status = Status.Ok
  val template: String = getClass.getName.replace('.', '/')
}

object View {

  /**
    * Use this View to perform a redirect in place of a standard View. This is commmonly used after a HTTP form POST.
    */
  case class Redirect(newLocation: String, override val status: Status = Status.SeeOther) extends View

  implicit def viewToFuture(view: View): Future[View] = Future(view)
}