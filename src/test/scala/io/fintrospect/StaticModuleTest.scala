package io.fintrospect

import com.twitter.finagle.Filter
import com.twitter.finagle.http.path.Root
import com.twitter.finagle.http.{Request, Status}
import com.twitter.util.Await
import org.scalatest.{FunSpec, ShouldMatchers}

class StaticModuleTest extends FunSpec with ShouldMatchers {

  it("looks up contents of existing root file") {
    val module = StaticModule(Root / "svc")
    val result = Await.result(module.toService(Request("/svc/mybob.xml")))
    result.status shouldEqual Status.Ok
    result.contentString shouldEqual "<xml>content</xml>"
    result.contentType.map(_.split(";")(0)) shouldEqual Option(ContentTypes.APPLICATION_XML.value)
  }

  it("looks up contents of existing subdir file") {
    val module = StaticModule(Root / "svc")
    val result = Await.result(module.toService(Request("/svc/io/fintrospect/StaticModule.js")))
    result.status shouldEqual Status.Ok
    result.contentString shouldEqual "function hearMeNow() { }"
    result.contentType.map(_.split(";")(0)) shouldEqual Option(ContentType("application/javascript").value)
  }

  it("can alter the root path") {
    val module = StaticModule(Root / "svc", "io/fintrospect")
    val result = Await.result(module.toService(Request("/svc/StaticModule.js")))
    result.status shouldEqual Status.Ok
    result.contentString shouldEqual "function hearMeNow() { }"
    result.contentType.map(_.split(";")(0)) shouldEqual Option(ContentType("application/javascript").value)
  }

  it("can add a filter") {
    val module = StaticModule(Root / "svc", "io/fintrospect", Filter.mk {
      (request, svc) => svc(request).map(rsp => {
        rsp.setStatusCode(Status.ExpectationFailed.code)
        rsp
      })
    })
    val result = Await.result(module.toService(Request("/svc/StaticModule.js")))
    result.status shouldEqual Status.ExpectationFailed
    result.contentString shouldEqual "function hearMeNow() { }"
    result.contentType.map(_.split(";")(0)) shouldEqual Option(ContentType("application/javascript").value)
  }

  it("looks up non existent-file") {
    val module = StaticModule(Root / "svc")
    val result = Await.result(module.toService(Request("/svc/NotHere.xml")))
    result.status shouldEqual Status.NotFound
  }

  it("cannot serve the root") {
    val module = StaticModule(Root / "svc")
    val result = Await.result(module.toService(Request("/")))
    result.status shouldEqual Status.NotFound
  }

  it("looks up non existent path") {
    val module = StaticModule(Root / "svc")
    val result = Await.result(module.toService(Request("/bob/StaticModule.js")))
    result.status shouldEqual Status.NotFound
  }

  it("can't subvert the path") {
    val module = StaticModule(Root / "svc")
    Await.result(module.toService(Request("/svc/../svc/Bob.xml"))).status shouldEqual Status.NotFound
    Await.result(module.toService(Request("/svc/~/.bashrc"))).status shouldEqual Status.NotFound
  }
}
