package io.fintrospect.testing

import com.twitter.finagle.http.Method.Get
import com.twitter.finagle.http.Status.{Accepted, Conflict}
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.finagle.{Http, Service}
import com.twitter.util.{Await, Future}
import io.fintrospect.RouteSpec
import org.scalatest.{BeforeAndAfterEach, FunSpec, Matchers}


class TestHttpServerTest extends FunSpec with Matchers with BeforeAndAfterEach {
  val originalStatus = Conflict

  private val server = new TestHttpServer(9888, RouteSpec().at(Get) bindTo Service.mk { r: Request => Future.value(Response(originalStatus)) })

  override def beforeEach() = {
    Await.result(server.start())
  }

  override def afterEach() = {
    Await.result(server.stop())
  }

  it("will serve routes that are passed to it") {
    statusShouldBe(originalStatus)
  }

  it("can override status") {
    server.respondWith(Accepted)
    statusShouldBe(Accepted)
  }

  private def statusShouldBe(expected: Status): Unit = {
    Await.result(Http.newService("localhost:9888", "")(Request())).status shouldBe expected
  }
}
