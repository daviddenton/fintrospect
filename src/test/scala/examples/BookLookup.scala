package examples

import com.twitter.finagle.Service
import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.Future
import io.github.daviddenton.fintrospect.ContentTypes._
import io.github.daviddenton.fintrospect.util.ResponseBuilder
import org.jboss.netty.handler.codec.http.HttpMethod
import org.jboss.netty.handler.codec.http.HttpMethod._
import ResponseBuilder._
import io.github.daviddenton.fintrospect._
import io.github.daviddenton.fintrospect.parameters.Path
import org.jboss.netty.handler.codec.http.HttpResponseStatus._

class BookLookup(books: Books) extends Route {

  private def lookupByIsbn(isbn: String): Service[Request, Response] = new Service[Request, Response] {
    override def apply(request: Request): Future[Response] =
      books.lookup(isbn) match {
        case Some(book) => Ok(book.toJson)
        case _ => Error(NOT_FOUND, "No book found with isbn")
      }
  }

  PathWrapper(HttpMethod.GET) / "hello" / "bob" / Path.int("bob2")

  def attachTo(module: FintrospectModule): FintrospectModule = {
    module.withRoute(
      Description("lookup book by isbn number")
        .producing(APPLICATION_JSON)
        .returning(NOT_FOUND -> "no book was found with this ISBN", ResponseWithExample.ERROR_EXAMPLE)
        .returning(OK -> "we found your book", Book("a book", "authorName", 99).toJson),
      On(GET, _ / "book"), Path.string("isbn", "the isbn of the book"), lookupByIsbn)
  }
}

