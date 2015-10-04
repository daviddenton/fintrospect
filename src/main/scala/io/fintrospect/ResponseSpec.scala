package io.fintrospect

import com.twitter.finagle.httpx.Status
import io.fintrospect.formats.json.{Argo, JsonFormat}
import io.fintrospect.parameters.BodySpec

import scala.util.Try
import scala.xml.Elem

/**
 * Defines a potential response from a route, with a possible example
 */
class ResponseSpec private[fintrospect](statusAndDescription: (Status, String), val example: Option[String] = None) {
  val status = statusAndDescription._1
  val description = statusAndDescription._2
}

object ResponseSpec {
  def json[T](statusAndDescription: (Status, String), example: T, jsonFormat: JsonFormat[T, _] = Argo.JsonFormat): ResponseSpec = {
    ResponseSpec(statusAndDescription, example, BodySpec.json(None, jsonFormat))
  }

  def xml(statusAndDescription: (Status, String), example: Elem): ResponseSpec = {
    ResponseSpec(statusAndDescription, example, BodySpec.xml())
  }

  def apply(statusAndDescription: (Status, String)): ResponseSpec = new ResponseSpec(statusAndDescription)

  def apply[T](statusAndDescription: (Status, String), example: T, bodySpec: BodySpec[T]): ResponseSpec = {
    new ResponseSpec(statusAndDescription, Try(bodySpec.serialize(example)).toOption)
  }
}