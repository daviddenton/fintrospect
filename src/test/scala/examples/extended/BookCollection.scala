package examples.extended

import com.twitter.finagle.Service
import com.twitter.util.Future
import io.fintrospect.ContentTypes._
import io.fintrospect._
import io.fintrospect.util.ArgoUtil._
import io.fintrospect.util.JsonResponseBuilder.Ok
import io.fintrospect.util.ResponseBuilder._
import org.jboss.netty.handler.codec.http.HttpMethod._
import org.jboss.netty.handler.codec.http.HttpResponseStatus._
import org.jboss.netty.handler.codec.http.{HttpRequest, HttpResponse}

class BookCollection(books: Books) {

  private def listBooks(): Service[HttpRequest, HttpResponse] = new Service[HttpRequest, HttpResponse] {
    override def apply(request: HttpRequest): Future[HttpResponse] = Ok(array(books.list().map(_.toJson)))
  }

  val route = HttpRoute("show collection")
    .producing(APPLICATION_JSON)
    .returning(OK -> "list of books", array(Book("a book", "authorName", 99).toJson))
    .at(GET) / "book" bindTo listBooks
}

