package examples.complete

import com.twitter.finagle.Service
import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.Future
import io.github.daviddenton.fintrospect.MimeTypes._
import io.github.daviddenton.fintrospect.parameters.Query
import io.github.daviddenton.fintrospect.parameters.Requirement.Mandatory
import io.github.daviddenton.fintrospect._
import org.jboss.netty.handler.codec.http.HttpMethod._
import org.jboss.netty.handler.codec.http.HttpResponseStatus._
import _root_.util.ResponseBuilder._

class BookSearch(books: Books) extends RouteSpec {
  private val authorQuery = Query.int("maxPages", "max number of pages in book", Mandatory)
  private val titleQuery = Query.string("term", "the part of the title to look for", Mandatory)

  private def search(): Service[Request, Response] = new Service[Request, Response] {
    override def apply(request: Request): Future[Response] = {
      val r = (for {
        author <- authorQuery.from(request)
        title <- titleQuery.from(request)
      } yield {
        Ok(books.search(author, title).mkString(","))
      }).getOrElse(Error(BAD_REQUEST, "not all parameters were specified"))
      r.toFuture
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
