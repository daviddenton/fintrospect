package io.fintrospect.templating

import com.twitter.finagle.http.Status

case class OnClasspath(items: Seq[Item], override val status: Status = Status.Ok) extends View
