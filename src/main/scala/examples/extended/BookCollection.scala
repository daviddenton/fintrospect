package examples.extended

import com.twitter.finagle.Service
import com.twitter.finagle.http.Method.Get
import com.twitter.finagle.http.Status.Ok
import com.twitter.finagle.http.{Request, Response, Status}
import io.fintrospect.ContentTypes.APPLICATION_JSON
import io.fintrospect.RouteSpec
import io.fintrospect.formats.Argo.JsonFormat.array
import io.fintrospect.formats.Argo.ResponseBuilder.implicits.statusToResponseBuilderConfig


class BookCollection(books: Books) {

  private def listBooks() = Service.mk[Request, Response] { _ => Ok(array(books.list().map(_.toJson))) }

  val route = RouteSpec("show collection")
    .producing(APPLICATION_JSON)
    .returning(Status.Ok -> "list of books", array(Book("a book", "authorName", 99).toJson))
    .at(Get) / "book" bindTo listBooks
}

