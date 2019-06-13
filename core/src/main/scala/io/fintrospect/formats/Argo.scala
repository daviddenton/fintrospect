package io.fintrospect.formats

import java.math.BigInteger

import argo.format.{CompactJsonFormatter, PrettyJsonFormatter}
import argo.jdom.JsonNodeFactories.booleanNode
import argo.jdom.{JdomParser, JsonNode, JsonNodeFactories}

/**
  * Argo JSON support (application/json content type)
  */
object Argo extends JsonLibrary[JsonNode, JsonNode] {

  object JsonFormat extends JsonFormat[JsonNode, JsonNode] {
    private val pretty = new PrettyJsonFormatter()
    private val compact = new CompactJsonFormatter()

    override def parse(in: String): JsonNode = new JdomParser().parse(in)

    override def pretty(node: JsonNode): String = pretty.format(node)

    override def compact(node: JsonNode): String = compact.format(node)

    override def obj(fields: Iterable[Field]): JsonNode = JsonNodeFactories.`object`(fields.map(f => field(f._1, f._2)).toSeq: _*)

    override def array(elements: Iterable[JsonNode]): JsonNode = JsonNodeFactories.array(elements.toSeq: _*)

    override def string(value: String) = JsonNodeFactories.string(value)

    override def number(value: Int) = JsonNodeFactories.number(value)

    override def number(value: Double) = JsonNodeFactories.number(BigDecimal(value).bigDecimal)

    override def number(value: BigDecimal) = JsonNodeFactories.number(value.bigDecimal)

    override def number(value: Long) = JsonNodeFactories.number(value)

    override def number(value: BigInteger) = JsonNodeFactories.number(value)

    override def boolean(value: Boolean) = booleanNode(value)

    override def nullNode() = JsonNodeFactories.nullNode()

    private def field(name: String, value: JsonNode) = JsonNodeFactories.field(name, value)
  }

}
