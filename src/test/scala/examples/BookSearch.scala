package examples

import com.twitter.finagle.Service
import com.twitter.util.Future
import io.github.daviddenton.fintrospect.ContentTypes.APPLICATION_JSON
import io.github.daviddenton.fintrospect._
import io.github.daviddenton.fintrospect.parameters.{Form, Query}
import io.github.daviddenton.fintrospect.util.ArgoUtil._
import io.github.daviddenton.fintrospect.util.ResponseBuilder._
import org.jboss.netty.handler.codec.http.HttpMethod._
import org.jboss.netty.handler.codec.http.HttpResponseStatus._
import org.jboss.netty.handler.codec.http.{HttpRequest, HttpResponse}

class BookSearch(books: Books) {
  private val maxPages = Query.optional.int("maxPages", "max number of pages in book")
  private val minPages = Form.optional.int("minPages", "min number of pages in book")
  private val titleTerm = Form.required.string("term", "the part of the title to look for")

  private def search() = new Service[HttpRequest, HttpResponse] {
    override def apply(request: HttpRequest): Future[HttpResponse] = {
      Ok(array(books.search(minPages.from(request).getOrElse(Integer.MIN_VALUE), maxPages.from(request).getOrElse(Integer.MAX_VALUE), titleTerm.from(request)).map(_.toJson)))
    }
  }

  val route = DescribedRoute("search for books")
    .taking(maxPages)
    .taking(minPages)
    .taking(titleTerm)
    .returning(OK -> "we found your book", array(Book("a book", "authorName", 99).toJson))
    .returning(OK -> "results", BAD_REQUEST -> "invalid request")
    .producing(APPLICATION_JSON)
    .at(POST) / "search" bindTo search
}
