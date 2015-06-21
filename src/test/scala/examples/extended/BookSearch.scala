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
  private val maxPages = Query.optional(ParameterSpec.int("maxPages", "max number of pages in book"))
  private val minPages = FormField.optional(ParameterSpec.int("minPages", "min number of pages in book"))
  private val titleTerm = FormField.required(ParameterSpec.string("term", "the part of the title to look for"))
  private val form = Body.form(minPages, titleTerm)


  private def search() = new Service[HttpRequest, HttpResponse] {
    override def apply(request: HttpRequest): Future[HttpResponse] = {
      Ok(array(books.search(minPages.from(form.from(request)).getOrElse(MIN_VALUE),
        maxPages.from(request).getOrElse(MAX_VALUE),
        titleTerm.from(form.from(request))).map(_.toJson)))
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
