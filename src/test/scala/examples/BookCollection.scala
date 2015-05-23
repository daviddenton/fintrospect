package examples

import com.twitter.finagle.Service
import com.twitter.util.Future
import io.github.daviddenton.fintrospect.ContentTypes._
import io.github.daviddenton.fintrospect._
import io.github.daviddenton.fintrospect.util.ArgoUtil._
import io.github.daviddenton.fintrospect.util.ResponseBuilder._
import org.jboss.netty.handler.codec.http.HttpMethod._
import org.jboss.netty.handler.codec.http.HttpResponseStatus._
import org.jboss.netty.handler.codec.http.{HttpRequest, HttpResponse}

class BookCollection(books: Books) {

  private def listBooks(): Service[HttpRequest, HttpResponse] = new Service[HttpRequest, HttpResponse] {
    override def apply(request: HttpRequest): Future[HttpResponse] = Json.Ok(array(books.list().map(_.toJson)))
  }

  val route = DescribedRoute("show collection")
    .producing(APPLICATION_JSON)
    .returning(OK -> "list of books", array(Book("a book", "authorName", 99).toJson))
    .at(GET) / "book" bindTo listBooks
}

