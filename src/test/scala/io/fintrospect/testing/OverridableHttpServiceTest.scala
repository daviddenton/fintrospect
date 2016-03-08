package io.fintrospect.testing

import com.twitter.finagle.{Service, Http}
import com.twitter.finagle.http.{Response, Request, Status}
import com.twitter.finagle.http.Status._
import com.twitter.util.{Future, Await}
import org.scalatest.{ShouldMatchers, FunSpec}

class OverridableHttpServiceTest extends FunSpec with ShouldMatchers {

  val originalStatus = Conflict

  val overridableHttpService = new OverridableHttpService[Request](Service.mk { r: Request => Future.value(Response(originalStatus)) })
  it("will serve routes that are passed to it") {
    statusShouldBe(originalStatus)
  }

  it("can override status") {
    overridableHttpService.respondWith(Accepted)
    statusShouldBe(Accepted)
  }

  private def statusShouldBe(expected: Status): Unit = {
    Await.result(overridableHttpService.service(Request())).status shouldBe expected
  }
}
