package io.fintrospect

import com.twitter.finagle.Service
import com.twitter.finagle.http.Method.Get
import com.twitter.finagle.http.path.Path
import com.twitter.finagle.http.{Method, Request, Response, Status}
import com.twitter.util.Await.result
import io.fintrospect.util.Echo
import io.fintrospect.util.HttpRequestResponseUtil.statusAndContentFrom
import org.scalatest.{FunSpec, Matchers}

class ModuleTest extends FunSpec with Matchers {

  describe("Module") {
    it("when it matches it responds as expected") {
      val response = statusAndContentFrom(result(routingWhichMatches((Get, Path("/someUrl")))(Request("/someUrl?field=hello"))))
      response._1 shouldBe Status.Ok
      response._2 should include("/someUrl?field=hello")
    }
    it("no match responds with default 404") {
      val response = result(routingWhichMatches((Get, Path("/someUrl")))(Request("/notMyService")))
      response.status shouldBe Status.NotFound
    }
  }

  private def routingWhichMatches(methodAndPath: (Method, Path)): Service[Request, Response] = {
    Module.toService(new PartialFunction[(Method, Path), Service[Request, Response]] {
      override def isDefinedAt(x: (Method, Path)): Boolean = x === methodAndPath

      override def apply(v1: (Method, Path)): Service[Request, Response] = Echo()
    })
  }
}