package io.fintrospect

import _root_.util.Echo
import com.twitter.finagle.Service
import com.twitter.finagle.http.Method._
import com.twitter.finagle.http.path.Path
import com.twitter.finagle.http.{Method, Request, Response, Status}
import com.twitter.util.Await._
import io.fintrospect.util.HttpRequestResponseUtil._
import org.scalatest.{FunSpec, ShouldMatchers}

class RoutingTest extends FunSpec with ShouldMatchers {

  describe("Routing") {
    it("when it matches it responds as expected") {
      val response = statusAndContentFrom(result(routingWhichMatches((Get, Path("/someUrl")))(Request("/someUrl?field=hello"))))
      response._1 shouldEqual Status.Ok
      response._2 should include("/someUrl?field=hello")
    }
    it("no match responds with default 404") {
      val response = result(routingWhichMatches((Get, Path("/someUrl")))(Request("/notMyService")))
      response.status shouldEqual Status.NotFound
    }
  }

  private def routingWhichMatches(methodAndPath: (Method, Path)): Routing = {
    Routing.fromBinding(new PartialFunction[(Method, Path), Service[Request, Response]] {
      override def isDefinedAt(x: (Method, Path)): Boolean = x === methodAndPath

      override def apply(v1: (Method, Path)): Service[Request, Response] = Echo()
    })
  }
}