package examples.extended

import java.lang.Integer.MIN_VALUE

import com.twitter.finagle.Service
import com.twitter.finagle.http.Method.Post
import com.twitter.finagle.http.{Request, Status}
import io.fintrospect.ContentTypes.APPLICATION_JSON
import io.fintrospect.RouteSpec
import io.fintrospect.formats.Argo.JsonFormat.array
import io.fintrospect.formats.Argo.ResponseBuilder._
import io.fintrospect.parameters.{Body, FormField}

class BookLengthSearch(books: Books) {
  private val minPages = FormField.optional.int("minPages", "min number of pages in book")
  private val maxPages = FormField.required.int("maxPages", "max number of pages in book")
  private val form = Body.form(minPages, maxPages)

  private val search = Service.mk {
    request: Request => {
      val requestForm = form <-- request
      Ok(array(books.search(minPages <-- requestForm getOrElse MIN_VALUE, maxPages <-- requestForm, Seq("")).map(_.toJson)))
    }
  }

  val route = RouteSpec("search for books by number of pages", "This won't work in Swagger because it's a form... :(")
    .body(form)
    .returning(Status.Ok -> "we found some books", array(Book("a book", "authorName", 99).toJson))
    .returning(Status.BadRequest -> "invalid request")
    .producing(APPLICATION_JSON)
    .at(Post) / "lengthSearch" bindTo search
}
