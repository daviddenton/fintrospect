package examples.extended

import java.lang.Integer._

import com.twitter.finagle.Service
import com.twitter.finagle.http.Method._
import com.twitter.finagle.http.{Response, Request}
import com.twitter.finagle.http.Status._
import io.fintrospect.ContentTypes.APPLICATION_JSON
import io.fintrospect._
import io.fintrospect.formats.ResponseBuilder._
import io.fintrospect.formats.json.Argo.JsonFormat._
import io.fintrospect.formats.json.Argo.ResponseBuilder._
import io.fintrospect.parameters._

class BookLengthSearch(books: Books) {
  private val minPages = FormField.optional.int("minPages", "min number of pages in book")
  private val maxPages = FormField.required.int("maxPages", "max number of pages in book")
  private val form = Body.form(minPages, maxPages)

  private def search() = Service.mk[Request, Response] {
    request => {
      val requestForm = form <-- request
      Ok(array(books.search(minPages <-- requestForm getOrElse MIN_VALUE, maxPages <-- requestForm, Seq("")).map(_.toJson)))
    }
  }

  val route = RouteSpec("search for books by number of pages", "This won't work in Swagger because it's a form... :(")
    .body(form)
    .returning(Ok -> "we found some books", array(Book("a book", "authorName", 99).toJson))
    .returning(BadRequest -> "invalid request")
    .producing(APPLICATION_JSON)
    .at(Post) / "lengthSearch" bindTo search
}
