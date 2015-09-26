package io.fintrospect.util.json

import java.math.BigInteger

import argo.format.{CompactJsonFormatter, PrettyJsonFormatter}
import argo.jdom.JsonNodeFactories._
import argo.jdom.{JdomParser, JsonNode, JsonNodeFactories, JsonRootNode}

/**
 * Utility functions for dealing with Argo JSON nodes (creating/parsing/printing)
 */
object ArgoJsonFormat extends JsonFormat[JsonRootNode, JsonNode] {

  private val pretty = new PrettyJsonFormatter()
  private val compact = new CompactJsonFormatter()

  override def parse(in: String): JsonRootNode = new JdomParser().parse(in)

  override def pretty(node: JsonRootNode): String = pretty.format(node)

  override def compact(node: JsonRootNode): String = compact.format(node)

  override def obj(fields: Iterable[Field]): JsonRootNode = {
    JsonNodeFactories.`object`(fields.map(f => field(f._1, f._2)).toSeq: _*)
  }

  def obj(fields: Field*): JsonRootNode = {
    JsonNodeFactories.`object`(fields.map(f => field(f._1, f._2)): _*)
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

  def field(name: String, value: JsonNode) = JsonNodeFactories.field(name, value)
}
