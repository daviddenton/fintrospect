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

class JsonToJsonSchema(input: JsonNode, idGen: () => String) {

  private def toSchema(input: JsonNode, definitions: List[JsonField]): (JsonRootNode, List[JsonField]) = {
    input.getType match {
      case NULL => throw new IllegalSchemaException("Cannot use a null value in a schema!")
      case STRING => (obj("type" -> string("string")), definitions)
      case TRUE => (obj("type" -> string("boolean")), definitions)
      case FALSE => (obj("type" -> string("boolean")), definitions)
      case NUMBER => (obj("type" -> string("number")), definitions)
      case ARRAY => {
        val (itemSchema, newDefinitions) = input.getElements.to[Seq].headOption.map(toSchema(_, definitions)).getOrElse(throw new IllegalSchemaException("Cannot use an empty list for a schema!"))
        (obj("type" -> string("array"), "items" -> itemSchema), newDefinitions)
      }
      case OBJECT => objectToSchema(input, definitions)
    }
  }

  private def objectToSchema(input: JsonNode, definitions: List[JsonField]): (JsonRootNode, List[JsonField]) = {
    val definitionId = idGen()

    val (finalFields, finalDefinitions) = input.getFieldList.to[Seq].foldLeft((List[(String, JsonRootNode)](), definitions)) {
      (memo, nextField) =>
        val next = toSchema(nextField.getValue, memo._2)
        (nextField.getName.getText -> next._1 :: memo._1, next._2)
    }

    val finalFinalDefinitions = field(definitionId, obj("type" -> string("object"), "properties" -> obj(finalFields: _*))) :: finalDefinitions
    (obj("$ref" -> string(s"#/definitions/$definitionId")), finalFinalDefinitions)
  }

  def toSchema(): (JsonRootNode, List[JsonField]) = toSchema(input, Nil)
}
