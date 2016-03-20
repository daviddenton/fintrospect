package examples.extended

import com.twitter.finagle.Service
import com.twitter.finagle.http.Status.{Conflict, Created}
import com.twitter.finagle.http.{Method, Request, Response}
import io.fintrospect.{ResponseSpec, RouteSpec}
import io.fintrospect.formats.ResponseBuilder.toFuture
import io.fintrospect.formats.json.Argo.ResponseBuilder.{toResponse, toResponseBuilder}
import io.fintrospect.parameters.{Body, Path}

class BookAdd(books: Books) {
  private val exampleBook = Book("the title", "the author", 666)
  private val bookExistsResponse = Conflict("Book with that ISBN exists")
  private val jsonBody = Body.json(Option("book content"), exampleBook.toJson)

  private def addBook(isbn: String) = Service.mk[Request, Response] {
    request =>
      books.lookup(isbn) match {
        case Some(_) => bookExistsResponse
        case None => {
          val book = Book.unapply(jsonBody <-- request).get
          books.add(isbn, book)
          Created(book.toJson)
        }
      }
  }

  val route = RouteSpec("add book by isbn number", "This book must not already exist")
    .body(jsonBody)
    .returning(ResponseSpec.json(Created -> "we added your book", exampleBook.toJson))
    .returning(bookExistsResponse)
    .at(Method.Post) / "book" / Path.string("isbn", "the isbn of the book") bindTo addBook
}


