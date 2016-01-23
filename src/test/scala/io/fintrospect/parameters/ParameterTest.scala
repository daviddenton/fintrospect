package io.fintrospect.parameters

import com.twitter.finagle.http.Request
import org.scalatest.{FunSpec, ShouldMatchers}

import scala.language.reflectiveCalls

class ParameterTest extends FunSpec with ShouldMatchers {

  describe("Parameter") {
    it("toString is descriptive") {
      Header.required.bigDecimal("paramName").toString shouldEqual "Parameter(name=paramName,where=header,paramType=number)"
    }

    it("custom type") {
      Query.required(MyCustomType) <-- Request("/?name=55") shouldEqual MyCustomType(55)
    }
  }

}
