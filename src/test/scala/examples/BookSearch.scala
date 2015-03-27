package examples

import _root_.util.ResponseBuilder._
import com.twitter.finagle.Service
import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.Future
import io.github.daviddenton.fintrospect.MimeTypes._
import io.github.daviddenton.fintrospect._
import io.github.daviddenton.fintrospect.parameters.Query
import io.github.daviddenton.fintrospect.util.ArgoUtil._
import org.jboss.netty.handler.codec.http.HttpMethod._
import org.jboss.netty.handler.codec.http.HttpResponseStatus._

class BookSearch(books: Books) extends RouteSpec {
  private val authorQuery = Query.required.int("maxPages", "max number of pages in book")
  private val titleQuery = Query.required.string("term", "the part of the title to look for")

  private def search(): Service[Request, Response] = new Service[Request, Response] {
    override def apply(request: Request): Future[Response] = {
      Ok(array(books.search(authorQuery.from(request), titleQuery.from(request)).map(_.toJson)))
    }
  }

  def attachTo(module: FintrospectModule): FintrospectModule = {
    module.withRoute(
      Description("search for books")
        .taking(authorQuery)
        .taking(titleQuery)
        .returning(OK -> "we found your book", array(Book("a book", "authorName", 99).toJson))
        .returning(OK -> "results", BAD_REQUEST -> "invalid request")
        .producing(TEXT_PLAIN),
      On(POST, _ / "search"), search)
  }
}
