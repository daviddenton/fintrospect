package experiments

import com.twitter.finagle.Filter
import com.twitter.finagle.http.Method.Get
import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.Future
import io.fintrospect.parameters.{Body, Query, Path => FPath}

import scala.xml.Elem

object ExpApp extends App {
  private val onePathOneParam = Contract0().taking(Query.required.string("a")).at(Get) / FPath.string("a")

  def svc0(c: String, params: (String, Request)) = Future[Response] {
    ???
  }

  onePathOneParam.bindTo(svc0)

  private val pathAndParams = Contract0()
    .taking(Query.required.string("a"))
    .body(Body.xml(Option("xmlBody")))
    .at(Get) / FPath.string("a") / FPath.boolean("a")

  def svc(c: String, b: Boolean, params: (String, Elem, Request)) = Future[Response] {
    val (str, int, req) = params
    ???
  }

  pathAndParams.bindTo(svc)

  private val pathOnly = Contract0().at(Get) / FPath.string("a") / FPath.boolean("a")

  def svc2(c: String, b: Boolean, req: Request) = Future[Response] {
    ???
  }

  pathOnly.bindTo(svc2)

  private val paramsOnly = Contract0()
    .withFilter(Filter.identity)
    .taking(Query.required.string("a")).taking(Query.required.int("a")).at(Get)

  def svc3(params: (String, Int, Request)) = Future[Response] {
    val (str, int, req) = params
    ???
  }

  paramsOnly.bindTo(svc3)

  private val nothing = Contract0().at(Get)

  def svc4(req: Request) = Future[Response] {
    ???
  }

  nothing.bindTo(svc4)

}
