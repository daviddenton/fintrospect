package cookbook.core

import io.fintrospect.ServerRoute
import io.fintrospect.parameters.Header.Optional
import io.fintrospect.parameters.Query.MandatorySeq

// fintrospect-core
object Simple_Request_Parameters_Example extends App {

  import com.twitter.finagle.http.Method.Get
  import com.twitter.finagle.http.path.Root
  import com.twitter.finagle.http.{Request, Response}
  import com.twitter.finagle.{Http, Service}
  import com.twitter.util.Await.ready
  import io.fintrospect.formats.PlainText.ResponseBuilder._
  import io.fintrospect.parameters.{Header, ParameterSpec, Query}
  import io.fintrospect.{Module, RouteModule, RouteSpec}

  import scala.language.reflectiveCalls

  val operatorSpec: ParameterSpec[(Int, Int) => Int] = ParameterSpec.string("operator").map {
    case "-" => (i: Int, j: Int) => i - j
    case "+" => (i: Int, j: Int) => i + j
  }

  val operator: Optional[(Int, Int) => Int] = Header.optional(operatorSpec)

  val values: MandatorySeq[Int] = Query.required.*.int("value")

  val calculate: Service[Request, Response] = Service.mk {
    (req: Request) => {
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