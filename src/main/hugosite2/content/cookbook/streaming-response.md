+++
title = "streaming responses"
tags = ["streaming", "finagle api"]
categories = ["recipe"]
intro = ""
+++

```scala

// fintrospect-core
object Streaming_Response_Example extends App {

  import argo.jdom.JsonRootNode
  import com.twitter.concurrent.AsyncStream
  import com.twitter.finagle.http.Method.Get
  import com.twitter.finagle.http.path.Root
  import com.twitter.finagle.http.{Request, Response}
  import com.twitter.finagle.{Http, Service}
  import com.twitter.util.Await.ready
  import com.twitter.util.Duration.fromSeconds
  import com.twitter.util.Future.sleep
  import com.twitter.util.JavaTimer
  import io.fintrospect.formats.Argo.JsonFormat._
  import io.fintrospect.formats.Argo.ResponseBuilder._
  import io.fintrospect.{Module, RouteModule, RouteSpec, ServerRoute}

  val timer = new JavaTimer()

  def ints(i: Int): AsyncStream[Int] = i +:: AsyncStream.fromFuture(sleep(fromSeconds(1))(timer)).flatMap(_ => ints(i + 1))

  def jsonStream: AsyncStream[JsonRootNode] = ints(0).map(i => obj("number" -> number(i)))

  val count: Service[Request, Response] = Service.mk[Request, Response] { req => Ok(jsonStream) }

  val route: ServerRoute[Request, Response] = RouteSpec().at(Get) bindTo count

  val module: Module = RouteModule(Root).withRoute(route)

  ready(Http.serve(":9999", module.toService))
}

//curl --raw http://localhost:9999/
```