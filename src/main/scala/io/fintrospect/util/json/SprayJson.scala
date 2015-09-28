package io.fintrospect.util.json

import java.math.BigInteger

import spray.json.DefaultJsonProtocol._
import spray.json.{JsValue, JsonParser, _}

/**
 * Argo JSON support.
 */
object SprayJson extends JsonLibrary[JsValue, JsValue] {

  lazy val JsonFormat = new JsonFormat[JsValue, JsValue] {

    override def parse(in: String) = JsonParser(in)

    override def pretty(node: JsValue): String = node.prettyPrint

    override def compact(node: JsValue): String = node.compactPrint

    override def obj(fields: Iterable[Field]) = JsObject(fields.toList: _*)

    override def obj(fields: Field*) = JsObject(fields: _*)

    override def array(elements: Iterable[JsValue]) = elements.toJson

    override def array(elements: JsValue*) = elements.toJson

    override def string(value: String) = value.toJson

    override def number(value: Int) = value.toJson

    override def number(value: BigDecimal) = value.toJson

    override def number(value: Long) = value.toJson

    override def number(value: BigInteger) = value.intValue().toJson

    override def boolean(value: Boolean) = value.toJson

    override def nullNode() = JsNull
  }

}
