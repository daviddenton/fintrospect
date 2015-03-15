package examples.complete

import _root_.util.ResponseBuilder._
import com.twitter.finagle.Service
import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.Future
import io.github.daviddenton.fintrospect.MimeTypes._
import io.github.daviddenton.fintrospect._
import org.jboss.netty.handler.codec.http.HttpMethod._
import org.jboss.netty.handler.codec.http.HttpResponseStatus._

class BookCollection(books: Books) extends RouteSpec {

  private def list(): Service[Request, Response] = new Service[Request, Response] {
    override def apply(request: Request): Future[Response] = Ok(books.list().mkString(","))
  }

  def attachTo(module: FintrospectModule): FintrospectModule = {
    module.withRoute(
      Description("show collection")
        .producing(TEXT_PLAIN)
        .returning(OK -> "list of books"),
      On(GET, _ / "book"), list)
  }
}

