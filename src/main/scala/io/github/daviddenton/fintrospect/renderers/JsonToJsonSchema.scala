package io.github.daviddenton.fintrospect.renderers

import argo.jdom.JsonNodeFactories.string
import argo.jdom.JsonNodeType._
import argo.jdom.{JsonField, JsonNode, JsonRootNode}
import io.github.daviddenton.fintrospect.util.ArgoUtil._

import scala.collection.JavaConversions._

/*
{
  "Pet": {
    "properties": {
      "id": {
        "type": "integer",
        "format": "int64"
      },
      "category": {"$ref": "#/definitions/Category"},
      "name": {
        "type": "string",
        "example": "doggie"
      },
      "photoUrls": {
        "type": "array",
        "xml": {
          "name": "photoUrl",
          "wrapped": true
        },
        "items": {"type": "string"}
      },
      "tags": {
        "type": "array",
        "xml": {
          "name": "tag",
          "wrapped": true
        },
        "items": {"$ref": "#/definitions/Tag"}
      },
      "status": {
        "type": "string",
        "description": "pet status in the store",
        "enum": [
          "available",
          "pending",
          "sold"
        ]
      }
    },
    "xml": {"name": "Pet"}
  }

 */

object JsonToJsonSchema {

  class IllegalSchemaException(message: String) extends Exception(message)

  case class ModelsAndNodes(definitions: List[JsonField] = Nil, nodes: List[JsonNode] = Nil)

  private def fieldToSchema(modelsAndNodes: ModelsAndNodes, input: JsonNode): ModelsAndNodes = {
    input.getType match {
      case STRING => modelsAndNodes.copy(nodes = obj("type" -> string("string")) :: modelsAndNodes.nodes)
      case TRUE => modelsAndNodes.copy(nodes = obj("type" -> string("boolean")) :: modelsAndNodes.nodes)
      case FALSE => modelsAndNodes.copy(nodes = obj("type" -> string("boolean")) :: modelsAndNodes.nodes)
      case NUMBER => modelsAndNodes.copy(nodes = obj("type" -> string("number")) :: modelsAndNodes.nodes)
      case ARRAY => {
        //        val obj: Any = obj("type" -> string("array"), "items" -> input.getElements.to[Seq].headOption.map {
        //          f => fieldToSchema(definitions, f)
        //        }).getOrElse(throw new IllegalSchemaException("Cannot use an empty list for a schema!")))
        modelsAndNodes
      }
      case OBJECT => modelsAndNodes.copy(
        nodes = obj("$ref" -> string("#/definitions/Tag")) :: modelsAndNodes.nodes)
      case NULL => throw new IllegalSchemaException("Cannot use a null value for a schema!")
    }
  }

  def toSchema(input: JsonNode): (List[JsonField], JsonNode) = {
    val allModelsAndNodes = input.getFieldList.to[Seq].foldLeft(ModelsAndNodes()) {
      (modelsAndNodes, next) => fieldToSchema(modelsAndNodes, next.getValue)
    }
    (allModelsAndNodes.definitions, obj("properties" -> obj(allModelsAndNodes.nodes: _*)))
  }
}
