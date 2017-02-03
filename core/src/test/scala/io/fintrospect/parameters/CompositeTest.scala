package io.fintrospect.parameters

import com.twitter.finagle.http.Method.Get
import com.twitter.finagle.http.Request
import io.fintrospect.RequestBuilder
import io.fintrospect.util.{Extracted, Extraction, ExtractionError, ExtractionFailed}
import org.scalatest.{FunSpec, Matchers}

class CompositeTest extends FunSpec with Matchers {

  case class FooBar(foo: String, bar: Int)

  object FooBar extends Composite[FooBar] {
    val foo = add(Query.required.string("foo"))
    val bar = add(Query.required.int("bar"))

    override def -->(foobar: FooBar): Iterable[Binding] = (foo --> foobar.foo) ++ (bar --> foobar.bar)

    override def <--?(req: Request): Extraction[FooBar] = {
      for {
        foo <- foo <--? req
        bar <- bar <--? req
      } yield FooBar(foo.get, bar.get)
    }
  }

  describe("Composite") {
    it("extraction from request succeeds") {
      FooBar <--? Request("?foo=foo&bar=123") shouldBe Extracted(Some(FooBar("foo", 123)))
    }
    it("extraction from request for invalid field fails") {
      FooBar <--? Request("?foo=foo") shouldBe ExtractionFailed(ExtractionError.Missing(FooBar.bar))
    }
    it("reports all fields") {
      FooBar.toSeq shouldBe Seq(FooBar.foo, FooBar.bar)
    }
    it("supports binding") {
      val req = (FooBar --> FooBar("foo", 123)).foldLeft(RequestBuilder(Get)) { (requestBuild, next) => next(requestBuild) }.build()
      req.uri shouldBe "/?foo=foo&bar=123"
    }
  }
}
