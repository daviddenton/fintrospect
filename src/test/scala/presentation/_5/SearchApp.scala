package presentation._5

import com.twitter.finagle.httpx.Method._
import com.twitter.finagle.httpx.filter.Cors
import com.twitter.finagle.httpx.path.Root
import com.twitter.finagle.httpx.{Request, Response, Status}
import com.twitter.finagle.{Httpx, Service}
import com.twitter.util.Future
import io.fintrospect._
import io.fintrospect.formats.json.Argo
import io.fintrospect.formats.json.Argo.JsonFormat._
import io.fintrospect.parameters.Query
import io.fintrospect.renderers.swagger2dot0.Swagger2dot0Json
import presentation.Book

class SearchRoute(books: RemoteBooks) {
  private val titlePartParam = Query.required.string("titlePart")

  def search() = new Service[Request, Response] {
    override def apply(request: Request): Future[Response] = {
      val titlePart = titlePartParam <-- request

      books.search(titlePart)
        .map(results => results.split(",").map(Book(_)).toSeq)
        .map(books => Argo.ResponseBuilder.Ok(array(books.map(_.toJson))))
    }
  }

  val route = RouteSpec("search books")
    .taking(titlePartParam)
    .returning(ResponseSpec.json(Status.Ok -> "search results", array(Book("1984").toJson)))
    .at(Get) / "search" bindTo search
}


class SearchApp {
  private val apiInfo = ApiInfo("search some books", "1.0", Option("an api for searching our book collection"))

  val service = FintrospectModule(Root, Swagger2dot0Json(apiInfo))
    .withRoute(new SearchRoute(new RemoteBooks).route)
    .toService

  val searchService = new CorsFilter(Cors.UnsafePermissivePolicy).andThen(service)
  Httpx.serve(":9000", searchService)
}

