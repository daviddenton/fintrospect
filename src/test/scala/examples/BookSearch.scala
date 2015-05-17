package examples

import com.twitter.util.Future
import io.github.daviddenton.fintrospect.ContentTypes.APPLICATION_JSON
import io.github.daviddenton.fintrospect.FinagleTypeAliases.{FTRequest, FTResponse, FTService}
import io.github.daviddenton.fintrospect._
import io.github.daviddenton.fintrospect.parameters.{Form, Query}
import io.github.daviddenton.fintrospect.util.ArgoUtil._
import io.github.daviddenton.fintrospect.util.ResponseBuilder._
import org.jboss.netty.handler.codec.http.HttpMethod._
import org.jboss.netty.handler.codec.http.HttpResponseStatus._

class BookSearch(books: Books) {
  private val authorQuery = Query.required.int("maxPages", "max number of pages in book")
  private val titleTerm = Form.required.string("term", "the part of the title to look for")

  private def search() = new FTService {
    override def apply(request: FTRequest): Future[FTResponse] = {
      Ok(array(books.search(authorQuery.from(request), titleTerm.from(request)).map(_.toJson)))
    }
  }

  val route = DescribedRoute("search for books")
    .taking(authorQuery)
    .taking(titleTerm)
    .returning(OK -> "we found your book", array(Book("a book", "authorName", 99).toJson))
    .returning(OK -> "results", BAD_REQUEST -> "invalid request")
    .producing(APPLICATION_JSON)
    .at(POST) / "search" bindTo search
}
