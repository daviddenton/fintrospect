package examples.extended

import com.twitter.finagle.Service
import com.twitter.finagle.http.Method._
import com.twitter.finagle.http.Status._
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.util.Future
import io.fintrospect.ContentTypes._
import io.fintrospect._
import io.fintrospect.formats.ResponseBuilder._
import io.fintrospect.formats.json.Argo.JsonFormat._
import io.fintrospect.formats.json.Argo.ResponseBuilder._

class BookCollection(books: Books) {

  private def listBooks(): Service[Request, Response] = new Service[Request, Response] {
    override def apply(request: Request): Future[Response] = Ok(array(books.list().map(_.toJson)))
  }

  val route = RouteSpec("show collection")
    .producing(APPLICATION_JSON)
    .returning(Status.Ok -> "list of books", array(Book("a book", "authorName", 99).toJson))
    .at(Get) / "book" bindTo listBooks
}

