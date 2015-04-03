package examples

import _root_.util.ResponseBuilder
import com.twitter.finagle.Service
import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.Future
import org.jboss.netty.handler.codec.http.HttpMethod._
import org.jboss.netty.handler.codec.http.HttpResponseStatus
import util.ResponseBuilder._
import io.github.daviddenton.fintrospect._
import io.github.daviddenton.fintrospect.parameters.{Body, Path}

class BookAdd(books: Books) extends RouteSpec {
  private val exampleBook = Book("the title", "the author", 666)

  private val body = Body.json(Some("book content"), exampleBook.toJson)

  private def addBook(isbn: String): Service[Request, Response] = new Service[Request, Response] {
    override def apply(request: Request): Future[Response] =

      books.lookup(isbn) match {
        case Some(_) => Error(HttpResponseStatus.CONFLICT, "Book with that ISBN exists")
        case None => {
          val book = Book.unapply(body.from(request)).get
          books.add(isbn, book)
          ResponseBuilder().withCode(HttpResponseStatus.CREATED).withContent(book.toJson)
        }
      }
  }

  def attachTo(module: FintrospectModule): FintrospectModule = {
    module.withRoute(
      Description("add book by isbn number")
        .taking(body)
        .returning(ResponseWithExample(HttpResponseStatus.CREATED, "we added your book", exampleBook.toJson)),
      On(POST, _ / "book"), Path.string("isbn", "the isbn of the book"), addBook)
  }
}

