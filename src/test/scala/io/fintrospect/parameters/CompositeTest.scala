package io.fintrospect.parameters

import com.twitter.finagle.http.Request
import org.scalatest._

class CompositeTest extends FunSpec with ShouldMatchers {

  case class Example(a: Option[String], b: Int)

  describe("Composite") {

    val optionalString = Query.optional.string("name")
    val requiredInt = Query.required.int("name2")

    it("successfully extracts when all parameters present") {
      val c = Composite {
        request =>
          (for {
            str <- optionalString.validate(request).asRight
            int <- requiredInt.validate(request).asRight
          } yield Example(str, int.get)).fold(MissingOrInvalid[Example], Extracted(_))
      }

      c <--? Request("/?name=query1&name2=12") shouldBe Extracted(Example(Some("query1"), 12))
    }

    it("reports when not all parameters present") {
      val c = Composite {
        request =>
          (for {
            name <- optionalString.validate(request).asRight
            name2 <- requiredInt.validate(request).asRight
          } yield Example(name, name2.get)).fold(MissingOrInvalid[Example], Extracted(_))
      }

      c <--? Request("/?name=query1") shouldBe MissingOrInvalid(requiredInt)
    }
  }

}
