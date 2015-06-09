package io.fintrospect.parameters

import io.fintrospect.util.ArgoUtil._

import scala.language.{higherKinds, implicitConversions}

abstract class JsonSupportingParametersTest[T[_] <: Parameter[_], R[_] <: Retrieval[_]](parameters: Parameters[T, R]) extends ParametersTest[T, R](parameters) {

  describe("json") {
    val expected = obj("field" -> string("value"))
    it("retrieves a valid value") {
      from(parameters.json, Some(compact(expected))) shouldEqual Some(expected)
    }

    it("does not retrieve an invalid value") {
      from(parameters.json, Some("notJson")) shouldEqual None
    }

    it("serializes json correctly") {
      to(parameters.json, expected).value shouldEqual """{"field":"value"}"""
    }
  }
}
