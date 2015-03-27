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

case class SchemaAndDefinitions(schema: JsonRootNode, definitions: List[JsonField] = Nil)


object JsonToJsonSchema {

  class IllegalSchemaException(message: String) extends Exception(message)

  private def fieldToSchema(input: JsonNode): JsonNode = {
    input.getType match {
      case STRING => obj("type" -> string("string"))
      case TRUE => obj("type" -> string("boolean"))
      case FALSE => obj("type" -> string("boolean"))
      case NUMBER => obj("type" -> string("number"))
      case ARRAY => obj("type" -> string("array"), "items" -> input.getElements.to[Seq].headOption.map(fieldToSchema).getOrElse(throw new IllegalSchemaException("Cannot use an empty list for a schema!")))
      case OBJECT => obj("type" -> string("object"))
      case NULL => throw new IllegalSchemaException("Cannot use a null value for a schema!")
    }
  }

  def toSchema(input: JsonNode): SchemaAndDefinitions = {
    val schema = obj("properties" -> obj(input.getFieldList.to[Seq].map(f => f.getName.getText -> fieldToSchema(f.getValue)): _*))
    SchemaAndDefinitions(schema, Nil)
  }
}
