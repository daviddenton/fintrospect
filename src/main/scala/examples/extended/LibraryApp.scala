package examples.extended

import com.twitter.finagle.Http
import com.twitter.finagle.http.filter.Cors
import com.twitter.finagle.http.filter.Cors.HttpFilter
import com.twitter.finagle.http.path.Root
import com.twitter.util.Await
import io.fintrospect.renderers.simplejson.SimpleJson
import io.fintrospect.renderers.swagger2dot0.{ApiInfo, Swagger2dot0Json}
import io.fintrospect.{Module, ModuleSpec}

/**
 * This example shows the intended method for implementing a simple app using Fintrospect routes and modules, using
 * a Swagger 2.0 renderer. The Swagger API endpoint lives at the root of the module (in this case /library) instead
 * of /api-docs.
 *
 * Note the use of the CORS policy filter to allow the endpoints to be called by the Swagger UI. For normal usage,
 * use CORs settings that suit your particular use-case. This one allows any cross-domain traffic at all and is applied
 * to all routes in the module (by being passed as the optional param to the ModuleSpec constructor)
 */
object LibraryApp extends App {

  val apiInfo = ApiInfo("Library Example", "1.0", Option("A simple example of how to construct a Fintrospect module"))
  val renderer = Swagger2dot0Json(apiInfo) //  choose your renderer implementation

  val books = new Books()

  // use CORs settings that suit your particular use-case. This one allows any cross-domain traffic at all and is applied
  // to all routes in the module
  val globalCorsFilter = new HttpFilter(Cors.UnsafePermissivePolicy)

  val libraryModule = ModuleSpec(Root / "library", renderer)
    .withRoute(new BookAdd(books).route)
    .withRoute(new BookCollection(books).route)
    .withRoute(new BookLookup(books).route)
    .withRoute(new BookLengthSearch(books).route)
    .withRoute(new BookTermSearch(books).route)

  val statusModule = ModuleSpec(Root / "internal", SimpleJson())
    .withRoute(new Ping().route)

  println("See the service description at: http://localhost:8080/library")

  Await.ready(
    Http.serve(":8080", globalCorsFilter.andThen(Module.toService(libraryModule combine statusModule)))
  )
}

