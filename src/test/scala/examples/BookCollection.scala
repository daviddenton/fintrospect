package examples

import _root_.util.ResponseBuilder._
import com.twitter.finagle.Service
import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.Future
import io.github.daviddenton.fintrospect._
import io.github.daviddenton.fintrospect.util.ArgoUtil._
import org.jboss.netty.handler.codec.http.HttpMethod._
import org.jboss.netty.handler.codec.http.HttpResponseStatus._

class BookCollection(books: Books) extends RouteSpec {

  private def list(): Service[Request, Response] = new Service[Request, Response] {
    override def apply(request: Request): Future[Response] = Ok(array(books.list().map(_.toJson)))
  }

  def attachTo(module: FintrospectModule): FintrospectModule = {
    module.withRoute(
      Description("show collection")
        .producing(MimeTypes.APPLICATION_JSON)
        .returning(OK -> "list of books", array(Book("a book", "authorName", 99).toJson)),
      On(GET, _ / "book"), list)
  }
}

