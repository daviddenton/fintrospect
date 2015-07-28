package examples.extended

import java.lang.Integer._

import com.twitter.finagle.Service
import com.twitter.util.Future
import io.fintrospect.ContentTypes.APPLICATION_JSON
import io.fintrospect._
import io.fintrospect.parameters._
import io.fintrospect.util.ArgoUtil._
import io.fintrospect.util.JsonResponseBuilder.Ok
import io.fintrospect.util.ResponseBuilder._
import org.jboss.netty.handler.codec.http.HttpMethod._
import org.jboss.netty.handler.codec.http.HttpResponseStatus._
import org.jboss.netty.handler.codec.http.{HttpRequest, HttpResponse}

class BookLengthSearch(books: Books) {
  private val minPages = FormField.optional.int("minPages", "min number of pages in book")
  private val maxPages = FormField.required.int("maxPages", "max number of pages in book")
  private val form = Body.form(minPages, maxPages)

  private def search() = new Service[HttpRequest, HttpResponse] {
    override def apply(request: HttpRequest): Future[HttpResponse] = {
      val requestForm = form <-- request
      Ok(array(books.search(
        minPages <-- requestForm getOrElse MIN_VALUE,
        maxPages <-- requestForm,
        Seq("")).map
        (_.toJson)))
    }
  }

  val route = RouteSpec("search for books by number of pages")
    .body(form)
    .returning(OK -> "we found some books", array(Book("a book", "authorName", 99).toJson))
    .returning(OK -> "results", BAD_REQUEST -> "invalid request")
    .producing(APPLICATION_JSON)
    .at(POST) / "lengthSearch" bindTo search
}
