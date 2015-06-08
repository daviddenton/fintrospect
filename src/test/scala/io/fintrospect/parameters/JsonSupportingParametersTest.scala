package io.fintrospect.parameters

import io.fintrospect.util.ArgoUtil._

import scala.language.{higherKinds, implicitConversions}

abstract class JsonSupportingParametersTest[T[_] <: Parameter[_], R[_] <: Retrieval[_]](parameters: Parameters[T, R]) extends ParametersTest[T, R](parameters) {

  describe("json") {
    it("retrieves a valid value") {
      val expected = obj("field" -> string("value"))
      from(parameters.json, Some(compact(expected))) shouldEqual Some(expected)
    }

    it("does not retrieve an invalid value") {
      from(parameters.json, Some("notJson")) shouldEqual None
    }
  }
}
