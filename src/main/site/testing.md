# testing

### routes
Provided trait ```TestingFintrospectRoute``` can be used to unit test your routes, as in the simple example below: 
```
object EchoRoute {
  val route = RouteSpec().at(Method.Get) / Path.string("message") bindTo( (message: String) => Service.mk {
    req: Request => Future.value(PlainText.ResponseBuilder.OK(message))
  })
}

class EchoRouteTest extends FunSpec with Matchers with TestingFintrospectRoute {

  override val route = EchoRoute.route

  describe("Echo") {
    it("bounces back message") {
      responseFor(Request("hello")).contentString shouldBe "hello"
    }
  }
}
```

### test http server
The ```TestHttpServer``` is convenient for attaching routes to during development, or can be used as a scaffold to provide automatically 
generated fake servers for downstream dependencies - simply complete the stub implementation of the server-side and fire it up. This works 
especially well if you are utilising custom serialisation formats (such as one of the auto-marshalling JSON libraries), as there is 
absolutely no marshalling code required to send back objects over the wire from your stub.

```
val route = RouteSpec().at(Get) / "myRoute" bindTo(() => Service.mk {r => Future.value(Response(Status.Ok))})
new TestHttpServer(9999, route).start()
```

<a class="next" href="http://fintrospect.io/examples"><button type="button" class="btn btn-sm btn-default">next: examples</button></a>
