+++
title = "simple request parameters"
tags = ["getting started", "request", "contract"]
categories = ["recipe"]
+++

```scala

// fintrospect-core
object Simple_Request_Parameters_Example extends App {

  import com.twitter.finagle.http.Method.Get
  import com.twitter.finagle.http.path.Root
  import com.twitter.finagle.http.{Request, Response}
  import com.twitter.finagle.{Http, Service}
  import com.twitter.util.Await.ready
  import io.fintrospect.formats.PlainText.ResponseBuilder._
  import io.fintrospect.parameters.Header.Optional
  import io.fintrospect.parameters.Query.MandatorySeq
  import io.fintrospect.parameters.{Header, ParameterSpec, Query}
  import io.fintrospect.{Module, RouteModule, RouteSpec, ServerRoute}

  val operatorSpec: ParameterSpec[(Int, Int) => Int] = ParameterSpec.string().map {
    case "-" => (i: Int, j: Int) => i - j
    case "+" => (i: Int, j: Int) => i + j
  }

  val operator: Optional[(Int, Int) => Int] = Header.optional(operatorSpec, "operator", "+ or -")

  val values: MandatorySeq[Int] = Query.required.*.int("value")

  val calculate: Service[Request, Response] = Service.mk[Request, Response] {
    req => {
      val components: Seq[Int] = values <-- req
      val op = operator <-- req
      Ok(s"the answer is ${components.fold(0)(op.getOrElse(_ + _))}" + " !")
    }
  }

  val route: ServerRoute[Request, Response] = RouteSpec().taking(operator).taking(values).at(Get) bindTo calculate

  val module: Module = RouteModule(Root).withRoute(route)

  ready(Http.serve(":9999", module.toService))
}

//curl -v -H"operator: +" http://localhost:9999?value=10&value=20&value=70
```