package io.fintrospect

import argo.jdom.JsonRootNode
import io.fintrospect.parameters.BodySpec
import org.jboss.netty.handler.codec.http.HttpResponseStatus

import scala.util.Try

case class ResponseWithExample(status: HttpResponseStatus, description: String, example: String)

object ResponseWithExample {
  def json(status: HttpResponseStatus, description: String, example: JsonRootNode): ResponseWithExample = {
    ResponseWithExample(status, description, example, BodySpec.json(Option(description)))
  }

  def apply[T](status: HttpResponseStatus, description: String, example: T, bodySpec: BodySpec[T]): ResponseWithExample = {
    ResponseWithExample(status, description, Try(bodySpec.serialize(example)).toOption.orNull)
  }
}