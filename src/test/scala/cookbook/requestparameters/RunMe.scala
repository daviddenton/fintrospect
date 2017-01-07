package cookbook.requestparameters

// fintrospect-core
object RunMe extends App {

  import com.twitter.finagle.http.Method.Get
  import com.twitter.finagle.http.path.Root
  import com.twitter.finagle.http.{Request, Response}
  import com.twitter.finagle.{Http, Service}
  import com.twitter.util.Await.ready
  import io.fintrospect.formats.PlainText.ResponseBuilder._
  import io.fintrospect.parameters.{Header, ParameterSpec, Query}
  import io.fintrospect.{RouteModule, RouteSpec}

  import scala.language.reflectiveCalls

  val operator = Header.optional(ParameterSpec.string("operator").map {
    case "-" => (i: Int, j: Int) => i - j
    case "+" => (i: Int, j: Int) => i + j
  })

  val values = Query.required.*.int("value")

  val calculate: Service[Request, Response] = Service.mk {
    (req: Request) => {
      val components: Seq[Int] = values <-- req
      val op = operator <-- req
      Ok(s"the answer is ${components.fold(1)(op.getOrElse(_ + _))}" + " !")
    }
  }

  val route = RouteSpec()
    .taking(operator)
    .taking(values)
    .at(Get) bindTo calculate

  val module: RouteModule[Request, Response] = RouteModule(Root).withRoute(route)

  ready(Http.serve(":9999", module.toService))
}
