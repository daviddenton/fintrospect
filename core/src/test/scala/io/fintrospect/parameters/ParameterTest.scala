package io.fintrospect.parameters

import com.twitter.finagle.http.Request
import org.scalatest.{FunSpec, Matchers}

import scala.language.reflectiveCalls

class ParameterTest extends FunSpec with Matchers {

  describe("Parameter") {
    it("toString is descriptive") {
      Header.required.bigDecimal("paramName").toString shouldBe "Mandatory parameter paramName (number) in header"
    }

    it("custom type") {
      Query.required(MyCustomType) <-- Request("/?name=55") shouldBe MyCustomType(55)
    }
  }

}
