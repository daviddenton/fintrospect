package io.github.daviddenton.fintrospect.util

import argo.format.PrettyJsonFormatter
import argo.jdom.JsonNodeFactories._
import argo.jdom.{JdomParser, JsonNode, JsonNodeFactories, JsonRootNode}

object ArgoUtil {

  private val pretty = new PrettyJsonFormatter()

  def parse(in: String): JsonRootNode = new JdomParser().parse(in)

  def pretty(node: JsonRootNode): String = pretty.format(node)

  def obj(fields: Iterable[(String, JsonNode)]): JsonRootNode = {
    JsonNodeFactories.`object`(fields.map(f => field(f._1, f._2)).toSeq: _*)
  }

  def obj(fields: (String, JsonNode)*): JsonRootNode = {
    JsonNodeFactories.`object`(fields.map(f => field(f._1, f._2)).toSeq: _*)
  }
}
