package examples

import com.twitter.finagle.Service
import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.Future
import io.github.daviddenton.fintrospect.util.ArgoUtil
import io.github.daviddenton.fintrospect.util.ArgoUtil.obj
import org.jboss.netty.handler.codec.http.HttpMethod._
import util.ResponseBuilder._
import io.github.daviddenton.fintrospect._
import io.github.daviddenton.fintrospect.parameters.Path
import org.jboss.netty.handler.codec.http.HttpResponseStatus._

class BookLookup(books: Books) extends RouteSpec {

  private def lookupByIsbn(isbn: String): Service[Request, Response] = new Service[Request, Response] {
    override def apply(request: Request): Future[Response] =
      books.lookup(isbn) match {
        case Some(book) => Ok(book.toJson)
        case _ => Error(NOT_FOUND, "No book found with isbn")
      }
  }

  def attachTo(module: FintrospectModule): FintrospectModule = {
    module.withRoute(
      Description("lookup book by isbn number")
        .producing(MimeTypes.APPLICATION_JSON)
        .returning(NOT_FOUND -> "no book was found with this ISBN", obj("message" -> ArgoUtil.string("someErrorMessage")))
        .returning(OK -> "we found your book", Book("a book", "authorName", 99).toJson),
      On(GET, _ / "book"), Path.string("isbn", "the isbn of the book"), lookupByIsbn)
  }
}

