package io.fintrospect.testing

import com.twitter.finagle.Service
import com.twitter.finagle.http.Status.{Accepted, Conflict}
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.util.{Await, Future}
import org.scalatest.{FunSpec, Matchers}

class OverridableHttpServiceTest extends FunSpec with Matchers {

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
