package presentation._3

import com.twitter.finagle.http.filter.Cors
import com.twitter.finagle.http.path.Root
import com.twitter.finagle.{Http, Service}
import com.twitter.util.Future
import io.fintrospect.formats.PlainTextResponseBuilder._
import io.fintrospect.formats.ResponseBuilder._
import io.fintrospect.formats.{PlainTextResponseBuilder, ResponseBuilder}
import io.fintrospect.parameters.Query
import io.fintrospect.renderers.swagger2dot0.Swagger2dot0Json
import io.fintrospect.{ApiInfo, CorsFilter, FintrospectModule, RouteSpec}
import org.jboss.netty.handler.codec.http.{HttpMethod, HttpRequest, HttpResponse}
import presentation.Books

class SearchRoute(books: Books) {
  private val titlePartParam = Query.required.string("titlePart")

  def search() = new Service[HttpRequest, HttpResponse] {
    override def apply(request: HttpRequest): Future[HttpResponse] = {
      val titlePart = titlePartParam <-- request
      val results = books.titles().filter(_.toLowerCase.contains(titlePart.toLowerCase))
      Ok(results.toString())
    }
  }

  val route = RouteSpec("search books")
    .taking(titlePartParam)
    .at(HttpMethod.GET) / "search" bindTo search
}


class SearchApp(books: Books) {
  private val apiInfo = ApiInfo("search some books", "1.0", Option("an api for searching our book collection"))

  val service = FintrospectModule(Root, Swagger2dot0Json(apiInfo))
    .withRoute(new SearchRoute(books).route)
    .toService

  val searchService = new CorsFilter(Cors.UnsafePermissivePolicy).andThen(service)
  Http.serve(":9000", searchService)
}


object Environment extends App {
  new SearchApp(new Books)
  Thread.currentThread().join()
}

/**
 * showcase: missing query param and successful search
 */
