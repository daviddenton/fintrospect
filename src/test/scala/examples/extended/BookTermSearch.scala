package examples.extended

import java.lang.Integer._

import com.twitter.finagle.Service
import com.twitter.util.Future
import io.fintrospect.ContentTypes.APPLICATION_JSON
import io.fintrospect._
import io.fintrospect.parameters._
import io.fintrospect.util.ResponseBuilder._
import io.fintrospect.util.json.ArgoJsonFormat._
import io.fintrospect.util.json.ArgoJsonResponseBuilder.Ok
import io.fintrospect.util.json.{ArgoJsonFormat, ArgoJsonResponseBuilder}
import org.jboss.netty.handler.codec.http.HttpMethod._
import org.jboss.netty.handler.codec.http.HttpResponseStatus._
import org.jboss.netty.handler.codec.http.{HttpRequest, HttpResponse}

class BookTermSearch(books: Books) {
  private val titleTerms = Query.required.*.string("term", "parts of the title to look for")

  private def search() = new Service[HttpRequest, HttpResponse] {
    override def apply(request: HttpRequest): Future[HttpResponse] = {
      Ok(array(books.search(
        MIN_VALUE,
        MAX_VALUE,
        titleTerms <-- request).map
        (_.toJson)))
    }
  }

  val route = RouteSpec("search for book by title fragment")
    .taking(titleTerms)
    .returning(OK -> "we found some books", array(Book("a book", "authorName", 99).toJson))
    .producing(APPLICATION_JSON)
    .at(GET) / "titleSearch" bindTo search
}
