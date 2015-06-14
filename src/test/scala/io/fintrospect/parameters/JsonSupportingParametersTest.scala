package io.fintrospect.parameters

import io.fintrospect.util.ArgoUtil._

import scala.language.{higherKinds, implicitConversions}
import scala.util.Success

abstract class JsonSupportingParametersTest[T[_] <: Parameter[_], R[_] <: Retrieval[_]](parameters: Parameters[T, R])
  extends ParametersTest[T, R](parameters) {

  describe("json") {
    val expected = obj("field" -> string("value"))
    it("retrieves a valid value") {
      attemptFrom(parameters.json, Some(compact(expected))) shouldEqual Some(Success(expected))
    }

    it("does not retrieve an invalid value") {
      attemptFrom(parameters.json, Some("notJson")).get.isFailure shouldEqual true
    }

    it("serializes json correctly") {
      to(parameters.json, expected).value shouldEqual """{"field":"value"}"""
    }
  }
}
