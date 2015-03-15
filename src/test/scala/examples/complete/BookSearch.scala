package examples.complete

import _root_.util.ResponseBuilder._
import com.twitter.finagle.Service
import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.Future
import io.github.daviddenton.fintrospect.MimeTypes._
import io.github.daviddenton.fintrospect._
import io.github.daviddenton.fintrospect.parameters.Query
import org.jboss.netty.handler.codec.http.HttpMethod._
import org.jboss.netty.handler.codec.http.HttpResponseStatus._

class BookSearch(books: Books) extends RouteSpec {
  private val authorQuery = Query.int("maxPages", "max number of pages in book")
  private val titleQuery = Query.string("term", "the part of the title to look for")

  private def search(): Service[Request, Response] = new Service[Request, Response] {
    override def apply(request: Request): Future[Response] = {
      (for {
        author <- authorQuery.from(request)
        title <- titleQuery.from(request)
      } yield {
        Ok(books.search(author, title).mkString(","))
      }).getOrElse(Error(BAD_REQUEST, "not all parameters were specified")).toFuture
    }
  }

  def attachTo(module: FintrospectModule): FintrospectModule = {
    module.withRoute(
      Description("search for books")
        .requiring(authorQuery)
        .requiring(titleQuery)
        .returning(OK -> "results", BAD_REQUEST -> "invalid request")
        .producing(TEXT_PLAIN),
      On(POST, _ / "search"), search)
  }
}
