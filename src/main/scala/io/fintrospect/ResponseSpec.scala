package io.fintrospect

import io.fintrospect.parameters.BodySpec
import io.fintrospect.util.json.{Argo, JsonFormat}
import org.jboss.netty.handler.codec.http.HttpResponseStatus

import scala.util.Try
import scala.xml.Elem

/**
 * Defines a potential response from a route, with a possible example
 */
class ResponseSpec private[fintrospect](statusAndDescription: (HttpResponseStatus, String), val example: Option[String] = None) {
  val status = statusAndDescription._1
  val description = statusAndDescription._2
}

object ResponseSpec {
  def json[T](statusAndDescription: (HttpResponseStatus, String), example: T, jsonFormat: JsonFormat[T, _ ,_] = Argo.JsonFormat): ResponseSpec = {
    ResponseSpec(statusAndDescription, example, BodySpec.json(None, jsonFormat))
  }

  def xml(statusAndDescription: (HttpResponseStatus, String), example: Elem): ResponseSpec = {
    ResponseSpec(statusAndDescription, example, BodySpec.xml())
  }

  def apply(statusAndDescription: (HttpResponseStatus, String)): ResponseSpec = new ResponseSpec(statusAndDescription)

  def apply[T](statusAndDescription: (HttpResponseStatus, String), example: T, bodySpec: BodySpec[T]): ResponseSpec = {
    new ResponseSpec(statusAndDescription, Try(bodySpec.serialize(example)).toOption)
  }
}