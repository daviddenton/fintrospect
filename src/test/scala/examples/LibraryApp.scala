package examples

import java.net.InetSocketAddress

import com.twitter.finagle.builder.ServerBuilder
import com.twitter.finagle.http.filter.Cors._
import com.twitter.finagle.http.path.Root
import com.twitter.finagle.http.{Http, Request, RichHttp}
import io.github.daviddenton.fintrospect._
import io.github.daviddenton.fintrospect.renderers.{SimpleJson, Swagger2dot0Json}

/**
 * This example shows the intended method for implementing a simple app using Fintrospect routes and modules, using
 * a Swagger 2.0 renderer. The Swagger API endpoint lives at the root of the module (in this case /library) instead
 * of /api-docs. Note the use of the CORS policy filter to allow the endpoints to be called by the Swagger UI.
 */
object LibraryApp extends App {

  private val apiInfo = ApiInfo("Library Example", "1.0", Some("A simple example of how to construct a Fintrospect module"))
  private val renderer: Renderer = Swagger2dot0Json(apiInfo) // choose your renderer implementation

  private val books = new Books()

  val libraryModule = FintrospectModule(Root / "library", renderer)
    .withRoute(new BookAdd(books).route)
    .withRoute(new BookCollection(books).route)
    .withRoute(new BookLookup(books).route)
    .withRoute(new BookSearch(books).route)

  val statusModule = FintrospectModule(Root / "internal", SimpleJson())
    .withRoute(new Ping().route)

  val service: FinagleTypeAliases.Service = FintrospectModule.toService(libraryModule combine statusModule)

  ServerBuilder()
    .codec(RichHttp[Request](Http()))
    .bindTo(new InetSocketAddress(8080))
    .name("")
    .build(new HttpFilter(UnsafePermissivePolicy).andThen(service))

  println("See the service description at: http://localhost:8080/library")
}

