package presentation._2

import com.twitter.finagle.httpx.filter.Cors
import com.twitter.finagle.httpx.path.Root
import com.twitter.finagle.httpx.{Method, Request, Response}
import com.twitter.finagle.{Httpx, Service}
import io.fintrospect.formats.ResponseBuilder._
import io.fintrospect.formats.text.PlainTextResponseBuilder._
import io.fintrospect.renderers.swagger2dot0.Swagger2dot0Json
import io.fintrospect.{ApiInfo, CorsFilter, FintrospectModule, RouteSpec}
import presentation.Books

class SearchApp(books: Books) {
  def search() = new Service[Request, Response] {
    override def apply(request: Request) = Ok(books.titles().toString())
  }

  private val apiInfo = ApiInfo("search some books", "1.0", Option("an api for searching our book collection"))

  val service = FintrospectModule(Root, Swagger2dot0Json(apiInfo))
    .withRoute(RouteSpec("search books").at(Method.Get) / "search" bindTo search)
    .toService

  val searchService = new CorsFilter(Cors.UnsafePermissivePolicy).andThen(service)
  Httpx.serve(":9000", searchService)
}


object Environment extends App {
  new SearchApp(new Books)
  Thread.currentThread().join()
}

/**
 * showcase: api and retrieve list of titles
 */
