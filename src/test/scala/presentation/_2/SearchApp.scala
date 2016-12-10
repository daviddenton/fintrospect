package presentation._2

import com.twitter.finagle.http.Method.Get
import com.twitter.finagle.http.Status.Ok
import com.twitter.finagle.http.filter.Cors
import com.twitter.finagle.http.filter.Cors.HttpFilter
import com.twitter.finagle.http.path.Root
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Http, Service}
import io.fintrospect.formats.PlainText.ResponseBuilder.implicits.statusToResponseBuilderConfig
import io.fintrospect.renderers.swagger2dot0.{ApiInfo, Swagger2dot0Json}
import io.fintrospect.{RouteModule, RouteSpec}
import presentation.Books

class SearchApp(books: Books) {
  def search() = Service.mk[Request, Response] { _ => Ok(books.titles().toString()) }

  private val apiInfo = ApiInfo("search some books", "1.0", Option("an api for searching our book collection"))

  val service = RouteModule(Root, Swagger2dot0Json(apiInfo))
    .withRoute(RouteSpec("search books").at(Get) / "search" bindTo search)
    .toService

  val searchService = new HttpFilter(Cors.UnsafePermissivePolicy).andThen(service)
  Http.serve(":9000", searchService)
}


object Environment extends App {
  new SearchApp(new Books)
  Thread.currentThread().join()
}

/**
  * showcase: api and retrieve list of titles
  */
