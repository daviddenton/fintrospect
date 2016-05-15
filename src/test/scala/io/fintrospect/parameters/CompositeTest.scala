package io.fintrospect.parameters

import com.twitter.finagle.http.Request
import org.scalatest._

class CompositeTest extends FunSpec with ShouldMatchers {

  case class Example(a: Option[String], b: Int)

  describe("Composite") {

    it("successfully extracts when all parameters present") {
      val c = Composite {
        request =>
          for {
            str <- Query.optional.string("name").validate(request).asRight
            int <- Query.required.int("name2").validate(request).asRight
          } yield Example(str, int.get)
      }

      c <--? Request("/?name=query1&name2=12") shouldBe Extracted(Example(Some("query1"), 12))
    }

    it("reports when not all parameters present") {
      val int = Query.required.int("name2")
      val c = Composite {
        request => for {
          name <- Query.optional.string("name").validate(request).asRight
          name2 <- int.validate(request).asRight
        } yield Example(name, name2.get)
      }

      c <--? Request("/?name=query1") shouldBe Invalid(int)
    }
  }

}
