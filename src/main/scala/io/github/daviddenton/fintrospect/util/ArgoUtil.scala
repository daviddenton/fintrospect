package io.github.daviddenton.fintrospect.util

import java.math.BigInteger

import argo.format.PrettyJsonFormatter
import argo.jdom.JsonNodeFactories._
import argo.jdom.{JdomParser, JsonNode, JsonNodeFactories, JsonRootNode}

object ArgoUtil {

  type Field = (String, JsonNode)

  private val pretty = new PrettyJsonFormatter()

  def parse(in: String): JsonRootNode = new JdomParser().parse(in)

  def pretty(node: JsonRootNode): String = pretty.format(node)

  def obj(fields: Iterable[Field]): JsonRootNode = {
    JsonNodeFactories.`object`(fields.map(f => field(f._1, f._2)).toSeq: _*)
  }

  def obj(fields: Field*): JsonRootNode = {
    JsonNodeFactories.`object`(fields.map(f => field(f._1, f._2)).toSeq: _*)
  }

  def array(elements: Iterable[JsonNode]) = JsonNodeFactories.array(elements.toSeq: _*)

  def array(elements: JsonNode*) = JsonNodeFactories.array(elements.toSeq: _*)

  def string(value: String) = JsonNodeFactories.string(value)

  def number(value: Int) = JsonNodeFactories.number(value)

  def number(value: BigDecimal) = JsonNodeFactories.number(value.bigDecimal)

  def number(value: Long) = JsonNodeFactories.number(value)

  def number(value: BigInteger) = JsonNodeFactories.number(value)

  def boolean(value: Boolean) = booleanNode(value)

  def nullNode() = JsonNodeFactories.nullNode()
}
