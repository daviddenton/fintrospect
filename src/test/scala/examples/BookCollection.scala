package examples

import io.github.daviddenton.fintrospect.FinagleTypeAliases.{FTService, FTResponse, FTRequest}
import io.github.daviddenton.fintrospect.util.ResponseBuilder
import ResponseBuilder._
import com.twitter.util.Future
import io.github.daviddenton.fintrospect.ContentTypes._
import io.github.daviddenton.fintrospect._
import io.github.daviddenton.fintrospect.util.ArgoUtil._
import org.jboss.netty.handler.codec.http.HttpMethod._
import org.jboss.netty.handler.codec.http.HttpResponseStatus._

class BookCollection(books: Books) {

  private def listBooks(): FTService = new FTService {
    override def apply(request: FTRequest): Future[FTResponse] = Ok(array(books.list().map(_.toJson)))
  }

  val route = DescribedRoute("show collection")
    .producing(APPLICATION_JSON)
    .returning(OK -> "list of books", array(Book("a book", "authorName", 99).toJson))
    .at(GET) / "book" bindTo listBooks
}

