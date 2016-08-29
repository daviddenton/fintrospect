package io.fintrospect

import com.twitter.finagle.Filter
import com.twitter.finagle.http.Status.{NotFound, Ok}
import com.twitter.finagle.http.path.Root
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.util.Await
import io.fintrospect.ResourceLoader.Classpath
import org.scalatest.{FunSpec, Matchers}

class StaticModuleTest extends FunSpec with Matchers {

  it("looks up contents of existing root file") {
    val module = StaticModule(Root / "svc")
    val result = Await.result(module.toService(Request("/svc/mybob.xml")))
    result.status shouldBe Ok
    result.contentString shouldBe "<xml>content</xml>"
    result.contentType.map(_.split(";")(0)) shouldBe Option(ContentTypes.APPLICATION_XML.value)
  }

  it("defaults to index.html if is no route") {
    val module = StaticModule(Root / "svc")
    val result = Await.result(module.toService(Request("/svc")))
    result.status shouldBe Ok
    result.contentString shouldBe "hello from the root index.html"
    result.contentType.map(_.split(";")(0)) shouldBe Option(ContentTypes.TEXT_HTML.value)
  }

  it("defaults to index.html if is no route - non-root-context") {
    val module = StaticModule(Root / "svc", Classpath("io"))
    val result = Await.result(module.toService(Request("/svc")))
    result.status shouldBe Ok
    result.contentString shouldBe "hello from the io index.html"
    result.contentType.map(_.split(";")(0)) shouldBe Option(ContentTypes.TEXT_HTML.value)
  }

  it("non existing index.html if is no route") {
    val module = StaticModule(Root / "svc", Classpath("io/fintrospect"))
    val result = Await.result(module.toService(Request("/svc")))
    result.status shouldBe NotFound
  }

  it("looks up contents of existing subdir file - non-root context") {
    val module = StaticModule(Root / "svc")
    val result = Await.result(module.toService(Request("/svc/io/fintrospect/StaticModule.js")))
    result.status shouldBe Ok
    result.contentString shouldBe "function hearMeNow() { }"
    result.contentType.map(_.split(";")(0)) shouldBe Option(ContentType("application/javascript").value)
  }

  it("looks up contents of existing subdir file") {
    val module = StaticModule(Root)
    val result = Await.result(module.toService(Request("/io/fintrospect/StaticModule.js")))
    result.status shouldBe Ok
    result.contentString shouldBe "function hearMeNow() { }"
    result.contentType.map(_.split(";")(0)) shouldBe Option(ContentType("application/javascript").value)
  }

  it("can alter the root path") {
    val module = StaticModule(Root / "svc", Classpath("io/fintrospect"))
    val result = Await.result(module.toService(Request("/svc/StaticModule.js")))
    result.status shouldBe Ok
    result.contentString shouldBe "function hearMeNow() { }"
    result.contentType.map(_.split(";")(0)) shouldBe Option(ContentType("application/javascript").value)
  }

  it("can add a filter") {
    val module = StaticModule(Root / "svc", Classpath("io/fintrospect"), Filter.mk[Request, Response, Request, Response] {
      (request, svc) => svc(request).map(rsp => {
        rsp.setStatusCode(Status.ExpectationFailed.code)
        rsp
      })
    })
    val result = Await.result(module.toService(Request("/svc/StaticModule.js")))
    result.status shouldBe Status.ExpectationFailed
    result.contentString shouldBe "function hearMeNow() { }"
    result.contentType.map(_.split(";")(0)) shouldBe Option(ContentType("application/javascript").value)
  }

  it("looks up non existent-file") {
    val module = StaticModule(Root / "svc", Classpath())
    val result = Await.result(module.toService(Request("/svc/NotHere.xml")))
    result.status shouldBe NotFound
  }

  it("cannot serve the root") {
    val module = StaticModule(Root / "svc", Classpath())
    val result = Await.result(module.toService(Request("/")))
    result.status shouldBe NotFound
  }

  it("looks up non existent path") {
    val module = StaticModule(Root / "svc")
    val result = Await.result(module.toService(Request("/bob/StaticModule.js")))
    result.status shouldBe NotFound
  }

  it("can't subvert the path") {
    val module = StaticModule(Root / "svc")
    Await.result(module.toService(Request("/svc/../svc/Bob.xml"))).status shouldBe NotFound
    Await.result(module.toService(Request("/svc/~/.bashrc"))).status shouldBe NotFound
  }
}
