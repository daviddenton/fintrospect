package examples.extended

import com.twitter.finagle.Service
import com.twitter.finagle.httpx.Method._
import com.twitter.finagle.httpx.{Status, _}
import com.twitter.util.Future
import io.fintrospect.ContentTypes._
import io.fintrospect._
import io.fintrospect.formats.ResponseBuilder._
import io.fintrospect.formats.json.Argo.ResponseBuilder._
import io.fintrospect.parameters.Path

class BookLookup(books: Books) {

  private def lookupByIsbn(isbn: String) = new Service[Request, Response] {
    override def apply(request: Request): Future[Response] =
      books.lookup(isbn) match {
        case Some(book) => Ok(book.toJson)
        case _ => Error(Status.NotFound, "No book found with isbn")
      }
  }

  val route = RouteSpec("lookup book by isbn number")
    .producing(APPLICATION_JSON)
    .returning(Error(Status.NotFound, "no book was found with this ISBN"))
    .returning(Status.Ok -> "we found your book", Book("a book", "authorName", 99).toJson)
    .at(Get) / "book" / Path.string("isbn", "the isbn of the book") bindTo lookupByIsbn
}

