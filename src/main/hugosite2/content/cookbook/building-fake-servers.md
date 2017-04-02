+++
title = "building fake servers"
tags = ["fake", "contract", "testing", "client"]
categories = ["recipe"]
intro = ""
+++

```scala
// fintrospect-core
object Building_Fake_Servers_Example extends App {

  import com.twitter.finagle.http.Method.Get
  import com.twitter.finagle.http.path.Root
  import com.twitter.finagle.http.{Request, Response}
  import com.twitter.finagle.{Http, ListeningServer, Service}
  import com.twitter.util.Await
  import io.fintrospect.formats.PlainText.ResponseBuilder.Ok
  import io.fintrospect.parameters.Path
  import io.fintrospect.{Module, RouteClient, RouteModule, RouteSpec, ServerRoute}

  object Contract {
    val name = Path.string("name")

    val route = RouteSpec().at(Get) / "sayHello" / name
  }

  def startFakeServer(): ListeningServer = {
    def sayHello(name: String): Service[Request, Response] = Service.mk[Request, Response] { req => Ok(s"hello $name!") }

    val route: ServerRoute[Request, Response] = RouteSpec().at(Get) / Contract.name bindTo say```scala
```
    val module: Module = RouteModule(Root).withRoute(route)

    Http.serve(":9999", module.toService)
  }

  def buildClient(): RouteClient[Response] = {
    val http: Service[Request, Response] = Http.newService("localhost:9999")
    Contract.route bindToClient http
  }

  startFakeServer()

  val client: RouteClient[Response] = buildClient()

  println(Await.result(client(Contract.name --> "David")).contentString)
}

//http://pokeapi.co/api/v2/pokemon/11/
```
