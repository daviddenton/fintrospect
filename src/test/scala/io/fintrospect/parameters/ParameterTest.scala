package io.fintrospect.parameters

import org.scalatest.{FunSpec, ShouldMatchers}

class ParameterTest extends FunSpec with ShouldMatchers {

  describe("Parameter") {
    it("toString is descriptive") {
      Header.required(ParameterSpec.bigDecimal("paramName")).toString shouldEqual "Parameter(name=paramName,where=header,paramType=number)"
    }
  }

}
