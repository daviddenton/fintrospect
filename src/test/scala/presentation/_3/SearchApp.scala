package presentation._3

import com.twitter.finagle.httpx.Method._
import com.twitter.finagle.httpx.filter.Cors
import com.twitter.finagle.httpx.path.Root
import com.twitter.finagle.httpx.{Request, Response}
import com.twitter.finagle.{Httpx, Service}
import com.twitter.util.Future
import io.fintrospect.formats.ResponseBuilder._
import io.fintrospect.formats.text.PlainTextResponseBuilder._
import io.fintrospect.parameters.Query
import io.fintrospect.renderers.swagger2dot0.Swagger2dot0Json
import io.fintrospect.{ApiInfo, CorsFilter, FintrospectModule, RouteSpec}
import presentation.Books

class SearchRoute(books: Books) {
  private val titlePartParam = Query.required.string("titlePart")

  def search() = new Service[Request, Response] {
    override def apply(request: Request): Future[Response] = {
      val titlePart = titlePartParam <-- request
      val results = books.titles().filter(_.toLowerCase.contains(titlePart.toLowerCase))
      OK(results.toString())
    }
  }

  val route = RouteSpec("search books")
    .taking(titlePartParam)
    .at(Get) / "search" bindTo search
}


class SearchApp(books: Books) {
  private val apiInfo = ApiInfo("search some books", "1.0", Option("an api for searching our book collection"))

  val service = FintrospectModule(Root, Swagger2dot0Json(apiInfo))
    .withRoute(new SearchRoute(books).route)
    .toService

  val searchService = new CorsFilter(Cors.UnsafePermissivePolicy).andThen(service)
  Httpx.serve(":9000", searchService)
}


object Environment extends App {
  new SearchApp(new Books)
  Thread.currentThread().join()
}

/**
 * showcase: missing query param and successful search
 */
