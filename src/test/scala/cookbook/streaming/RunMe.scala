package cookbook.streaming

import com.twitter.util.JavaTimer

object RunMe extends App {

  import com.twitter.concurrent.AsyncStream
  import com.twitter.finagle.http.Method.Get
  import com.twitter.finagle.http.path.Root
  import com.twitter.finagle.http.{Request, Response}
  import com.twitter.finagle.{Http, Service}
  import com.twitter.util.Await.ready
  import com.twitter.util.{Duration, Future}
  import io.fintrospect.formats.Xml.ResponseBuilder._
  import io.fintrospect.{RouteModule, RouteSpec, ServerRoute}

  val timer = new JavaTimer()

  def ints(i: Int): AsyncStream[Int] =
    i +:: AsyncStream.fromFuture(Future.sleep(Duration.fromSeconds(1))(timer)).flatMap(_ => ints(i + 1))

  val count: Service[Request, Response] = Service.mk[Request, Response] {
    req =>
      Ok(ints(0).map(i =>
        <xml>
          {i}
        </xml>)
      )
  }

  val route: ServerRoute[Request, Response] = RouteSpec()
    .at(Get) bindTo count

  val module: RouteModule[Request, Response] =
    RouteModule(Root)
      .withRoute(route)

  ready(Http.serve(":9999", module.toService))
}
