package examples

import com.twitter.finagle.Service
import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.Future
import io.github.daviddenton.fintrospect.util.ResponseBuilder
import org.jboss.netty.handler.codec.http.HttpMethod._
import org.jboss.netty.handler.codec.http.HttpResponseStatus._
import ResponseBuilder._
import io.github.daviddenton.fintrospect._
import io.github.daviddenton.fintrospect.parameters.{Body, Path}

class BookAdd(books: Books) {
  private val exampleBook = Book("the title", "the author", 666)
  private val bookExistsResponse = Error(CONFLICT, "Book with that ISBN exists")
  private val body = Body.json(Some("book content"), exampleBook.toJson)

  private def addBook(isbn: String): Service[Request, Response] = new Service[Request, Response] {
    override def apply(request: Request): Future[Response] =

      books.lookup(isbn) match {
        case Some(_) => bookExistsResponse
        case None => {
          val book = Book.unapply(body.from(request)).get
          books.add(isbn, book)
          ResponseBuilder().withCode(CREATED).withContent(book.toJson)
        }
      }
  }

  val route = Description("add book by isbn number")
    .taking(body)
    .returning(ResponseWithExample(CREATED, "we added your book", exampleBook.toJson))
    .returning(bookExistsResponse)
    .at(POST) / "book" / Path.string("isbn", "the isbn of the book") then addBook
}

