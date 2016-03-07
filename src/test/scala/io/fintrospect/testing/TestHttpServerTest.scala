package io.fintrospect.testing

import com.twitter.finagle.http.Method.Get
import com.twitter.finagle.http.Status._
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.finagle.{Http, Service}
import com.twitter.util.{Await, Future}
import io.fintrospect.RouteSpec
import org.scalatest.{BeforeAndAfterEach, FunSpec, ShouldMatchers}

class TestHttpServerTest extends FunSpec with ShouldMatchers with BeforeAndAfterEach {
  val originalStatus = Conflict

  private val server = new TestHttpServer(9888, RouteSpec().at(Get) bindTo Service.mk { r: Request => Future.value(Response(originalStatus)) })

  override def beforeEach() = {
    Await.result(server.start())
  }

  override def afterEach() = {
    Await.result(server.stop())
  }

  it("will serve routes that are passed to it") {
    statusFrom(originalStatus)
  }

  it("can override status") {
    server.respondWith(Accepted)
    statusFrom(Accepted)
  }

  private def statusFrom(expected: Status): Unit = {
    Await.result(Http.newService("localhost:9888", "")(Request())).status shouldBe expected
  }
}
