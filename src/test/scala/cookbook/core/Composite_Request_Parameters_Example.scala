package cookbook.core


// fintrospect-core
object Composite_Request_Parameters_Example extends App {

  import com.twitter.finagle.http.Method.Get
  import com.twitter.finagle.http.path.Root
  import com.twitter.finagle.http.{Request, Response}
  import com.twitter.finagle.{Http, Service}
  import com.twitter.util.Await.ready
  import io.fintrospect.formats.PlainText.ResponseBuilder._
  import io.fintrospect.parameters.{Binding, Composite, Header, Query}
  import io.fintrospect.util.Extraction
  import io.fintrospect.{Module, RouteModule, RouteSpec, ServerRoute}

  import scala.language.reflectiveCalls

  case class FooBar(foo: String, bar: Int)

  object FooBar extends Composite[FooBar] {
    private val fooQ = add(Header.required.string("foo"))
    private val barQ = add(Query.required.int("bar"))

    override def -->(foobar: FooBar): Iterable[Binding] = (fooQ --> foobar.foo) ++ (barQ --> foobar.bar)

    override def <--?(req: Request): Extraction[FooBar] = {
      for {
        foo <- fooQ <--? req
        bar <- barQ <--? req
      } yield FooBar(foo, bar)
    }
  }

  val route: ServerRoute[Request, Response] = RouteSpec()
    .taking(FooBar).at(Get) bindTo Service.mk {
    req: Request => Ok("you sent: " + (FooBar <-- req))
  }

  val module: Module = RouteModule(Root).withRoute(route)

  ready(Http.serve(":9999", module.toService))
}

//curl -v -H"foo: foo" http://localhost:9999?bar=123