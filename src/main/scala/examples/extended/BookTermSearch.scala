package examples.extended

import java.lang.Integer.{MAX_VALUE, MIN_VALUE}

import com.twitter.finagle.Service
import com.twitter.finagle.http.Method.Get
import com.twitter.finagle.http.Status.Ok
import com.twitter.finagle.http.{Request, Response}
import io.fintrospect.ContentTypes.APPLICATION_JSON
import io.fintrospect.RouteSpec
import io.fintrospect.formats.Argo.JsonFormat.array
import io.fintrospect.formats.Argo.ResponseBuilder.implicits.statusToResponseBuilderConfig
import io.fintrospect.parameters.Query

class BookTermSearch(books: Books) {
  private val titleTerms = Query.required.*.string("term", "parts of the title to look for")

  private def search() = Service.mk[Request, Response] { request => Ok(array(books.search(MIN_VALUE, MAX_VALUE, titleTerms <-- request).map(_.toJson))) }

  val route = RouteSpec("search for book by title fragment")
    .taking(titleTerms)
    .returning(Ok -> "we found some books", array(Book("a book", "authorName", 99).toJson))
    .producing(APPLICATION_JSON)
    .at(Get) / "titleSearch" bindTo search
}
