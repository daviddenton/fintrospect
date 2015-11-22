package examples.full.main

import com.twitter.finagle.http.Status._
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Service, SimpleFilter}
import io.fintrospect.formats.text.PlainTextResponseBuilder._

object CatchAll extends SimpleFilter[Request, Response] {
  override def apply(request: Request, service: Service[Request, Response]) =
    service(request)
      .handle {
        case e: RemoteSystemProblem => ServiceUnavailable(e.getMessage)
        case e: Exception => InternalServerError(e.getMessage)
      }
}