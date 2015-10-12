package examples.extended

import com.twitter.finagle.Httpx
import com.twitter.finagle.httpx.filter.Cors
import com.twitter.finagle.httpx.path.Root
import io.fintrospect._
import io.fintrospect.renderers.simplejson.SimpleJson
import io.fintrospect.renderers.swagger2dot0.{ApiInfo, Swagger2dot0Json}

/**
 * This example shows the intended method for implementing a simple app using Fintrospect routes and modules, using
 * a Swagger 2.0 renderer. The Swagger API endpoint lives at the root of the module (in this case /library) instead
 * of /api-docs. Note the use of the CORS policy filter to allow the endpoints to be called by the Swagger UI.
 */
object LibraryApp extends App {

  private val apiInfo = ApiInfo("Library Example", "1.0", Option("A simple example of how to construct a Fintrospect module"))
  private val renderer = Swagger2dot0Json(apiInfo) //  choose your renderer implementation

  private val books = new Books()

  val libraryModule = FintrospectModule(Root / "library", renderer)
    .withRoute(new BookAdd(books).route)
    .withRoute(new BookCollection(books).route)
    .withRoute(new BookLookup(books).route)
    .withRoute(new BookLengthSearch(books).route)
    .withRoute(new BookTermSearch(books).route)

  val statusModule = FintrospectModule(Root / "internal", SimpleJson())
    .withRoute(new Ping().route)

  val service = FintrospectModule.toService(libraryModule combine statusModule)

  Httpx.serve(":8080", new CorsFilter(Cors.UnsafePermissivePolicy).andThen(service))

  println("See the service description at: http://localhost:8080/library")

  Thread.currentThread().join()
}

