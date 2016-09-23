package presentation._5

import com.twitter.finagle.http.Method.Get
import com.twitter.finagle.http.Status.Ok
import com.twitter.finagle.http.filter.Cors
import com.twitter.finagle.http.filter.Cors.HttpFilter
import com.twitter.finagle.http.path.Root
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Http, Service}
import io.fintrospect.formats.Argo.JsonFormat.array
import io.fintrospect.formats.Argo.ResponseBuilder.implicits.{responseBuilderToResponse, statusToResponseBuilderConfig}
import io.fintrospect.parameters.Query
import io.fintrospect.renderers.swagger2dot0.{ApiInfo, Swagger2dot0Json}
import io.fintrospect.{ModuleSpec, ResponseSpec, RouteSpec}
import presentation.Book

class SearchRoute(books: RemoteBooks) {
  private val titlePartParam = Query.required.string("titlePart")

  def search() = Service.mk[Request, Response] {
    request => {
      val titlePart = titlePartParam <-- request

      books.search(titlePart)
        .map(results => results.split(",").map(Book(_)).toSeq)
        .map(books => Ok(array(books.map(_.toJson))))
    }
  }

  val route = RouteSpec("search books")
    .taking(titlePartParam)
    .returning(ResponseSpec.json(Ok -> "search results", array(Book("1984").toJson)))
    .at(Get) / "search" bindTo search
}


class SearchApp {
  private val apiInfo = ApiInfo("search some books", "1.0", Option("an api for searching our book collection"))

  val service = ModuleSpec(Root, Swagger2dot0Json(apiInfo))
    .withRoute(new SearchRoute(new RemoteBooks).route)
    .toService

  val searchService = new HttpFilter(Cors.UnsafePermissivePolicy).andThen(service)
  Http.serve(":9000", searchService)
}

