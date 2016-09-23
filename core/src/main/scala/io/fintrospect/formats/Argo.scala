package io.fintrospect.formats

import java.math.BigInteger

import argo.format.{CompactJsonFormatter, PrettyJsonFormatter}
import argo.jdom.JsonNodeFactories.booleanNode
import argo.jdom.{JdomParser, JsonNode, JsonNodeFactories, JsonRootNode}

/**
 * Argo JSON support (application/json content type)
 */
object Argo extends JsonLibrary[JsonRootNode, JsonNode] {
  object JsonFormat extends JsonFormat[JsonRootNode, JsonNode] {
    private val pretty = new PrettyJsonFormatter()
    private val compact = new CompactJsonFormatter()

    override def parse(in: String): JsonRootNode = new JdomParser().parse(in)

    override def pretty(node: JsonRootNode): String = pretty.format(node)

    override def compact(node: JsonRootNode): String = compact.format(node)

    override def obj(fields: Iterable[Field]): JsonRootNode = JsonNodeFactories.`object`(fields.map(f => field(f._1, f._2)).toSeq: _*)

    override def obj(fields: Field*): JsonRootNode = JsonNodeFactories.`object`(fields.map(f => field(f._1, f._2)): _*)

    override def array(elements: Iterable[JsonNode]) = JsonNodeFactories.array(elements.toSeq: _*)

    override def array(elements: JsonNode*) = JsonNodeFactories.array(elements.toSeq: _*)

    override def string(value: String) = JsonNodeFactories.string(value)

    override def number(value: Int) = JsonNodeFactories.number(value)

    override def number(value: BigDecimal) = JsonNodeFactories.number(value.bigDecimal)

    override def number(value: Long) = JsonNodeFactories.number(value)

    override  def number(value: BigInteger) = JsonNodeFactories.number(value)

    override def boolean(value: Boolean) = booleanNode(value)

    override def nullNode() = JsonNodeFactories.nullNode()

    private def field(name: String, value: JsonNode) = JsonNodeFactories.field(name, value)
  }

}
