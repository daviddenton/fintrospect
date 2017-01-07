package cookbook.streaming


object RunMe extends App {

  import com.twitter.concurrent.AsyncStream
  import com.twitter.finagle.http.Method.Get
  import com.twitter.finagle.http.path.Root
  import com.twitter.finagle.http.{Request, Response}
  import com.twitter.finagle.{Http, Service}
  import com.twitter.util.Await.ready
  import com.twitter.util.Duration.fromSeconds
  import com.twitter.util.Future.sleep
  import com.twitter.util.JavaTimer
  import io.circe.Json
  import io.fintrospect.formats.Circe.JsonFormat._
  import io.fintrospect.formats.Circe.ResponseBuilder._
  import io.fintrospect.{RouteModule, RouteSpec, ServerRoute}

  val timer = new JavaTimer()

  def ints(i: Int): AsyncStream[Int] = i +:: AsyncStream.fromFuture(sleep(fromSeconds(1))(timer)).flatMap(_ => ints(i + 1))

  def jsonStream: AsyncStream[Json] = ints(0).map(i => obj("number" -> number(i)))

  val count: Service[Request, Response] = Service.mk[Request, Response] { req => Ok(jsonStream) }

  val route: ServerRoute[Request, Response] = RouteSpec().at(Get) bindTo count

  val module: RouteModule[Request, Response] = RouteModule(Root).withRoute(route)

  ready(Http.serve(":9999", module.toService))
}
