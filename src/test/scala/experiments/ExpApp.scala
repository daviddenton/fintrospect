package experiments

import com.twitter.finagle.Filter
import com.twitter.finagle.http.Method.Get
import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.Future
import io.fintrospect.parameters.{Body, Query, Path => FPath}

import scala.xml.Elem

object ExpApp extends App {
  private val taking: Contract1[String] = Contract().taking(Query.required.string("a"))
  private val onePathOneParam: PathBuilder1[(String), String] = taking.at(Get) / FPath.string("a")

  def svc0(c: String, params: (String), req: Request) = Future[Response] {
    ???
  }

  onePathOneParam.bindTo(svc0)

  private val pathAndParams = Contract()
    .taking(Query.required.string("a"))
    .body(Body.xml(Option("xmlBody")))
    .at(Get) / FPath.string("a") / FPath.boolean("a")

  def svc(c: String, b: Boolean, params: (String, Elem), req: Request) = Future[Response] {
    val (str, int) = params
    ???
  }

  pathAndParams.bindTo(svc)


  private val pathOnly = Contract().at(Get) / FPath.string("a") / FPath.boolean("a")

  def svc2(c: String, b: Boolean, p: Unit, req: Request) = Future[Response] {
    ???
  }

  pathOnly.bindTo(svc2)

  private val paramsOnly = Contract()
    .withFilter(Filter.identity)
    .taking(Query.required.string("a")).taking(Query.required.int("a")).at(Get)

  def svc3(params: (String, Int), req: Request) = Future[Response] {
    val (str, int) = params
    ???
  }

  paramsOnly.bindTo(svc3)

  private val nothing = Contract().at(Get)

  def svc4(params: Unit, req: Request) = Future[Response] {
    ???
  }

  nothing.bindTo(svc4)

}
