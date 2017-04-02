+++
title = "patching endpoint"
tags = ["circe", "patch", "route"]
categories = ["recipe"]
intro = "asd"
+++

```scala

case class Employee(name: String, age: Option[Int])

// fintrospect-core
// fintrospect-circe
object Patching_Endpoint_Example extends App {

  import com.twitter.finagle.http.Method.Post
  import com.twitter.finagle.http.path.Root
  import com.twitter.finagle.http.{Request, Response}
  import com.twitter.finagle.{Http, Service}
  import com.twitter.util.Await.ready
  import com.twitter.util.Future
  import io.circe.generic.auto._
  import io.fintrospect.formats.Circe
  import io.fintrospect.formats.Circe.JsonFormat._
  import io.fintrospect.formats.Circe.ResponseBuilder._
  import io.fintrospect.parameters.Path
  import io.fintrospect.{Module, RouteModule, RouteSpec, ServerRoute}

  import scala.collection.mutable

  val employees = mutable.Map[Int, Employee](1 -> Employee("David", None))

  val patchBody = Circe.patchBody[Employee]()

  def updateAge(id: Int): Service[Request, Response] =
    Service.mk[Request, Response] {
      req =>
        val patcher = patchBody <-- req
        Future(employees.get(id) match {
          case Some(employee) =>
            employees(id) = patcher(employee)
            Ok(encode(employees.get(id)))
          case _ => NotFound(s"with id $id")
        })
    }

  val route: ServerRoute[Request, Response] = RouteSpec()
    .body(patchBody)
    .at(Post) / Path.int("id") bindTo updateAge

  val module: Module = RouteModule(Root).withRoute(route)

  ready(Http.serve(":9999", module.toService))
}

//curl -v -XPOST http://localhost:9999/1 --data '{"age": 50}'
```