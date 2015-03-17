package examples

import com.twitter.finagle.Service
import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.Future
import org.jboss.netty.handler.codec.http.HttpMethod._
import util.ResponseBuilder._
import io.github.daviddenton.fintrospect._
import io.github.daviddenton.fintrospect.parameters.Path
import org.jboss.netty.handler.codec.http.HttpResponseStatus._

class BookLookup(books: Books) extends RouteSpec {

  private def lookupByIsbn(isbn: String): Service[Request, Response] = new Service[Request, Response] {
    override def apply(request: Request): Future[Response] =
      books.lookup(isbn) match {
        case Some(book) => Ok(book.toString)
        case _ => Error(NOT_FOUND, "No book found with isbn")
      }
  }

  def attachTo(module: FintrospectModule): FintrospectModule = {
    module.withRoute(
      Description("lookup book by isbn number")
        .producing(MimeTypes.TEXT_PLAIN)
        .returning(OK -> "we found your book", NOT_FOUND -> "no book was found with this ISBN"),
      On(GET, _ / "book"), Path.string("isbn"), lookupByIsbn)
  }
}

