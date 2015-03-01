package io.github.daviddenton.fintrospect.util

import argo.format.{CompactJsonFormatter, PrettyJsonFormatter}
import argo.jdom.JsonNodeFactories._
import argo.jdom.{JsonNode, JsonNodeFactories, JsonRootNode}

object ArgoUtil {

  private val pretty = new PrettyJsonFormatter()
  private val compact = new CompactJsonFormatter()

  def pretty(node: JsonRootNode): String = pretty.format(node)

  def compact(node: JsonRootNode): String = compact.format(node)

  def toJson(fields: Seq[(String, String)]): JsonRootNode = toJson(fields.toMap)

  def toJson(fields: Map[String, String]): JsonRootNode = {
    JsonNodeFactories.`object`(fields.map(f => field(f._1, string(f._2))).toSeq: _*)
  }

  def obj(fields: Iterable[(String, JsonNode)]): JsonRootNode = {
    JsonNodeFactories.`object`(fields.map(f => field(f._1, f._2)).toSeq: _*)
  }

  def obj(fields: (String, JsonNode)*): JsonRootNode = {
    JsonNodeFactories.`object`(fields.map(f => field(f._1, f._2)).toSeq: _*)
  }
}
