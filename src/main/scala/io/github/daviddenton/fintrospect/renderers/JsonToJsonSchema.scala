package io.github.daviddenton.fintrospect.renderers

import argo.jdom.JsonNodeFactories.{field, string}
import argo.jdom.JsonNodeType._
import argo.jdom.{JsonField, JsonNode, JsonRootNode}
import io.github.daviddenton.fintrospect.renderers.JsonToJsonSchema.IllegalSchemaException
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

  def toSchema(input: JsonNode): JsonRootNode = {
    input.getType match {
      case NULL => throw new IllegalSchemaException("Cannot use a null value in a schema!")
      case STRING => obj("type" -> string("string"))
      case TRUE => obj("type" -> string("boolean"))
      case FALSE => obj("type" -> string("boolean"))
      case NUMBER => obj("type" -> string("number"))
      case ARRAY => obj("type" -> string("array"), "items" -> input.getElements.to[Seq].headOption.map(toSchema).getOrElse(throw new IllegalSchemaException("Cannot use an empty list for a schema!")))
      case OBJECT => obj("type" -> string("object"), "properties" -> obj(input.getFieldList.to[Seq].map(f => f.getName.getText -> toSchema(f.getValue)): _*))
    }
  }
}

case class Schema[T <: JsonNode](node: T, fields: List[JsonField])

class JsonToJsonSchema(input: JsonNode, idGen: () => String) {

  private def toSchema(input: Schema[JsonNode]): Schema[JsonRootNode] = {
    input.node.getType match {
      case NULL => throw new IllegalSchemaException("Cannot use a null value in a schema!")
      case STRING => Schema[JsonRootNode](obj("type" -> string("string")), input.fields)
      case TRUE => Schema[JsonRootNode](obj("type" -> string("boolean")), input.fields)
      case FALSE => Schema[JsonRootNode](obj("type" -> string("boolean")), input.fields)
      case NUMBER => Schema[JsonRootNode](obj("type" -> string("number")), input.fields)
      case ARRAY => {
        val headItemSchema = input.node.getElements.to[Seq].headOption.map(n => toSchema(Schema(n, input.fields))).getOrElse(throw new IllegalSchemaException("Cannot use an empty list for a schema!"))
        Schema[JsonRootNode](obj("type" -> string("array"), "items" -> headItemSchema.node), headItemSchema.fields)
      }
      case OBJECT => objectToSchema(input)
    }
  }

  private def objectToSchema(input: Schema[JsonNode]): Schema[JsonRootNode] = {
    val definitionId = idGen()

    val (finalFields, finalDefinitions) = input.node.getFieldList.to[Seq].foldLeft((List[(String, JsonRootNode)](), input.fields)) {
      (memo, nextField) =>
        val next = toSchema(Schema[JsonNode](nextField.getValue, memo._2))
        (nextField.getName.getText -> next.node :: memo._1, next.fields)
    }

    val finalFinalDefinitions = field(definitionId, obj("type" -> string("object"), "properties" -> obj(finalFields: _*))) :: finalDefinitions
    Schema[JsonRootNode](obj("$ref" -> string(s"#/definitions/$definitionId")), finalFinalDefinitions)
  }

  def toSchema(): Schema[JsonRootNode] = toSchema(Schema(input, Nil))
}
