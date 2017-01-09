package examples.extended

import java.lang.Integer.{MAX_VALUE, MIN_VALUE}

import com.twitter.finagle.Service
import com.twitter.finagle.http.Method.Get
import com.twitter.finagle.http.{Request, Status}
import io.fintrospect.ContentTypes.APPLICATION_JSON
import io.fintrospect.RouteSpec
import io.fintrospect.formats.Argo.JsonFormat.array
import io.fintrospect.formats.Argo.ResponseBuilder._
import io.fintrospect.parameters.Query

class BookTermSearch(books: Books) {
  private val titleTerms = Query.required.*.string("term", "parts of the title to look for")

  private val search = Service.mk { request: Request =>
    Ok(array(books.search(MIN_VALUE, MAX_VALUE, titleTerms <-- request).map(_.toJson)))
  }

  val route = RouteSpec("search for book by title fragment")
    .taking(titleTerms)
    .returning(Status.Ok -> "we found some books", array(Book("a book", "authorName", 99).toJson))
    .producing(APPLICATION_JSON)
    .at(Get) / "titleSearch" bindTo search
}
