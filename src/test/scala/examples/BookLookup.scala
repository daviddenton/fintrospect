package examples

import com.twitter.finagle.Service
import com.twitter.util.Future
import io.github.daviddenton.fintrospect.ContentTypes._
import io.github.daviddenton.fintrospect._
import io.github.daviddenton.fintrospect.parameters.Path
import io.github.daviddenton.fintrospect.util.JsonResponseBuilder.{Error, Ok}
import io.github.daviddenton.fintrospect.util.ResponseBuilder._
import org.jboss.netty.handler.codec.http.HttpMethod._
import org.jboss.netty.handler.codec.http.HttpResponseStatus._
import org.jboss.netty.handler.codec.http.{HttpRequest, HttpResponse}

class BookLookup(books: Books) {

  private def lookupByIsbn(isbn: String) = new Service[HttpRequest, HttpResponse] {
    override def apply(request: HttpRequest): Future[HttpResponse] =
      books.lookup(isbn) match {
        case Some(book) => Ok(book.toJson)
        case _ => Error(NOT_FOUND, "No book found with isbn")
      }
  }

  val route = DescribedRoute("lookup book by isbn number")
    .producing(APPLICATION_JSON)
    .returning(NOT_FOUND -> "no book was found with this ISBN", ResponseWithExample.ERROR_EXAMPLE)
    .returning(OK -> "we found your book", Book("a book", "authorName", 99).toJson)
    .at(GET) / "book" / Path.string("isbn", "the isbn of the book") bindTo lookupByIsbn
}

