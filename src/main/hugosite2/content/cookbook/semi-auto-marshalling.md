+++
title = "semi-auto body marshalling"
tags = ["body", "contract", "json", "filter", "auto"]
categories = ["fintrospect-core", "fintrospect-circe"]
intro = ""
+++

```scala

case class Person(name: String, age: Option[Int])

object Semi_Auto_Marshalling_Example extends App {

  import com.twitter.finagle.http.Method.Post
  import com.twitter.finagle.http.path.Root
  import com.twitter.finagle.http.{Request, Response}
  import com.twitter.finagle.{Http, Service}
  import com.twitter.util.Await.ready
  import io.circe.generic.auto._
  import io.fintrospect.formats.Circe
  import io.fintrospect.formats.Circe.JsonFormat._
  import io.fintrospect.formats.Circe.ResponseBuilder._
  import io.fintrospect.parameters.Body
  import io.fintrospect.{Module, RouteModule, RouteSpec, ServerRoute}

  val personBody = Body.of(Circe.bodySpec[Person]())

  val insultMe: Service[Request, Response] = Service.mk[Request, Response] { req =>
    val person: Person = personBody <-- req
    val smellyPerson: Person = person.copy(name = person.name + " Smells")
    Ok(encode(smellyPerson))
  }

  val route: ServerRoute[Request, Response] = RouteSpec()
    .body(personBody)
    .at(Post) bindTo insultMe

  val module: Module = RouteModule(Root).withRoute(route)

  ready(Http.serve(":9999", module.toService))
}

//curl -v -XPOST http://localhost:9999/ --data '{"name":"David", "age": 50}'
```