package examples.complete

import java.net.InetSocketAddress

import argo.jdom.JsonRootNode
import com.twitter.finagle.builder.ServerBuilder
import com.twitter.finagle.http.filter.Cors._
import com.twitter.finagle.http.path.Root
import com.twitter.finagle.http.{Http, Request, RichHttp}
import io.github.daviddenton.fintrospect._
import io.github.daviddenton.fintrospect.renderers.Swagger2dot0Json

object LibraryApp extends App {

  private val renderer: Renderer = Swagger2dot0Json() // choose your renderer implementation

  private val books = new Books()

  val module = FintrospectModule(Root, renderer)
    .withRouteSpec(new BookCollection(books))
    .withRouteSpec(new BookLookup(books))
    .withRouteSpec(new BookSearch(books))

  ServerBuilder()
    .codec(RichHttp[Request](Http()))
    .bindTo(new InetSocketAddress(8080))
    .name("")
    .build(new HttpFilter(UnsafePermissivePolicy).andThen(module.toService))

  println("See the service description at: http://localhost:8080")
}

