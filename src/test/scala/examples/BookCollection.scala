package examples

import io.github.daviddenton.fintrospect.util.ResponseBuilder
import ResponseBuilder._
import com.twitter.finagle.Service
import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.Future
import io.github.daviddenton.fintrospect.ContentTypes._
import io.github.daviddenton.fintrospect._
import io.github.daviddenton.fintrospect.util.ArgoUtil._
import org.jboss.netty.handler.codec.http.HttpMethod._
import org.jboss.netty.handler.codec.http.HttpResponseStatus._

class BookCollection(books: Books) extends Route {

  private def list(): Service[Request, Response] = new Service[Request, Response] {
    override def apply(request: Request): Future[Response] = Ok(array(books.list().map(_.toJson)))
  }

  def attachTo(module: FintrospectModule2): FintrospectModule2 = {
    module.withRoute(
      Description("show collection")
        .producing(APPLICATION_JSON)
        .returning(OK -> "list of books", array(Book("a book", "authorName", 99).toJson))
        .at(GET) / "book" then list)
  }
}

