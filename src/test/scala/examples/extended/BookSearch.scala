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

class BookSearch(books: Books) {
  private val maxPages = Query.optional.int("maxPages", "max number of pages in book")
  private val minPages = FormField.optional.int("minPages", "min number of pages in book")
  private val titleTerm = FormField.required.string("term", "the part of the title to look for")
  private val form = Body.form(minPages, titleTerm)

  private def search() = new Service[HttpRequest, HttpResponse] {
    override def apply(request: HttpRequest): Future[HttpResponse] = {
      val requestForm = form.from(request)
      Ok(array(books.search(minPages.from(requestForm).getOrElse(MIN_VALUE),
        maxPages.from(request).getOrElse(MAX_VALUE),
        titleTerm.from(requestForm)).map(_.toJson)))
    }
  }

  val route = DescribedRoute("search for books")
    .taking(maxPages)
    .body(form)
    .returning(OK -> "we found your book", array(Book("a book", "authorName", 99).toJson))
    .returning(OK -> "results", BAD_REQUEST -> "invalid request")
    .producing(APPLICATION_JSON)
    .at(POST) / "search" bindTo search
}
