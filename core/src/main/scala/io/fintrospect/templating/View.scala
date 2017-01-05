package io.fintrospect.templating

import com.twitter.util.Future

import scala.language.implicitConversions

/**
  * View model for use with templating system.
  */
trait View {
  val template: String = getClass.getName.replace('.', '/')
}

object View {
  implicit def viewToFuture(view: View): Future[View] = Future(view)
}