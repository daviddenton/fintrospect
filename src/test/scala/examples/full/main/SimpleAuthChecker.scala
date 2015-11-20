package examples.full.main

import com.twitter.finagle.http.Status._
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Service, SimpleFilter}
import io.fintrospect.formats.text.PlainTextResponseBuilder._
import io.fintrospect.parameters.Header

object SimpleAuthChecker {
  val key = Header.required.string("key")
}

class SimpleAuthChecker extends SimpleFilter[Request, Response] {
  override def apply(request: Request, service: Service[Request, Response]) = {
    if (SimpleAuthChecker.key.from(request) == "realSecret") service(request) else Unauthorized()
  }
}
