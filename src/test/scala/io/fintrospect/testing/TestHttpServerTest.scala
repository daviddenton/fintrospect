package io.fintrospect.testing

import com.twitter.finagle.http.Method.Get
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.finagle.{Http, Service}
import com.twitter.util.{Await, Future}
import io.fintrospect.RouteSpec
import org.scalatest.{FunSpec, ShouldMatchers}

class TestHttpServerTest extends FunSpec with ShouldMatchers {

  it("will serve routes that are passed to it") {
    val statusToReturn = Status.Conflict

    val server = new TestHttpServer(9888, RouteSpec().at(Get) bindTo (
      () => Service.mk { r => Future.value(Response(statusToReturn)) }))
    Await.result(server.start())

    Await.result(Http.newService("localhost:9888", "")(Request())).status shouldBe statusToReturn

    Await.result(server.stop())

  }
}
