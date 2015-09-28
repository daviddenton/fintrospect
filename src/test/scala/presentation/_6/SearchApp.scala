package presentation._6

import com.twitter.finagle.http.filter.Cors
import com.twitter.finagle.http.path.Root
import com.twitter.finagle.{Http, Service}
import com.twitter.util.Future
import io.fintrospect.ContentTypes._
import io.fintrospect._
import io.fintrospect.formats.PlainTextResponseBuilder
import io.fintrospect.parameters.{Body, BodySpec, Query, StringParamType}
import io.fintrospect.renderers.swagger2dot0.Swagger2dot0Json
import io.fintrospect.util.json.Argo
import io.fintrospect.util.json.Argo.JsonFormat._
import org.jboss.netty.handler.codec.http.{HttpMethod, HttpRequest, HttpResponse, HttpResponseStatus}
import presentation.Book


class SearchRoute(books: RemoteBooks) {
  private val titlePartParam = Query.required.string("titlePart")

  def search() = new Service[HttpRequest, HttpResponse] {
    override def apply(request: HttpRequest): Future[HttpResponse] = {
      val titlePart = titlePartParam <-- request

      books.search(titlePart)
        .map(results => results.split(",").map(Book(_)).toSeq)
        .map(books => Argo.ResponseBuilder.Ok(array(books.map(_.toJson))))
    }
  }

  val route = RouteSpec("search books")
    .taking(titlePartParam)
    .returning(ResponseSpec.json(HttpResponseStatus.OK -> "search results", array(Book("1984").toJson)))
    .at(HttpMethod.GET) / "search" bindTo search
}

class BookAvailable(books: RemoteBooks) {
  private val bodySpec = BodySpec[Book](Option("a book"), APPLICATION_JSON, s => Book.fromJson(parse(s)), b => compact(b.toJson))
  private val body = Body(bodySpec, Book("1984"), StringParamType)

  def availability() = new Service[HttpRequest, HttpResponse] {
    override def apply(request: HttpRequest): Future[HttpResponse] = {
      val book = body <-- request
      books.search(book.title)
        .map(results => {
        if (results.length > 0) PlainTextResponseBuilder.Ok else PlainTextResponseBuilder.Error(HttpResponseStatus.NOT_FOUND, "!")
      })
    }
  }

  val route = RouteSpec("find if the book is owned")
    .body(body)
    .returning(HttpResponseStatus.OK -> "book is available")
    .returning(HttpResponseStatus.NOT_FOUND -> "book not found")
    .at(HttpMethod.POST) / "availability" bindTo availability
}


class SearchApp {
  private val apiInfo = ApiInfo("search some books", "1.0", Option("an api for searching our book collection"))

  val service = FintrospectModule(Root, Swagger2dot0Json(apiInfo))
    .withRoute(new SearchRoute(new RemoteBooks).route)
    .withRoute(new BookAvailable(new RemoteBooks).route)
    .toService

  val searchService = new CorsFilter(Cors.UnsafePermissivePolicy).andThen(service)
  Http.serve(":9000", searchService)
}

