package io.github.daviddenton.fintrospect.renderers

import argo.jdom.JsonNodeFactories.string
import argo.jdom.JsonNodeType._
import argo.jdom.{JsonNode, JsonRootNode}
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

case class Schema(node: JsonNode, modelDefinitions: List[Field])

class JsonToJsonSchema(idGen: () => String) {

  private def toSchema(input: Schema): Schema = {
    input.node.getType match {
      case NULL => throw new IllegalSchemaException("Cannot use a null value in a schema!")
      case STRING => Schema(obj("type" -> string("string")), input.modelDefinitions)
      case TRUE => Schema(obj("type" -> string("boolean")), input.modelDefinitions)
      case FALSE => Schema(obj("type" -> string("boolean")), input.modelDefinitions)
      case NUMBER => Schema(obj("type" -> string("number")), input.modelDefinitions)
      case ARRAY => {
        val headItemSchema = input.node.getElements.to[Seq].headOption.map(n => toSchema(Schema(n, input.modelDefinitions))).getOrElse(throw new IllegalSchemaException("Cannot use an empty list for a schema!"))
        Schema(obj("type" -> string("array"), "items" -> headItemSchema.node), headItemSchema.modelDefinitions)
      }
      case OBJECT => objectToSchema(input)
    }
  }

  private def objectToSchema(input: Schema): Schema = {
    val definitionId = idGen()

    val (finalFields, finalDefinitions) = input.node.getFieldList.foldLeft((List[Field](), input.modelDefinitions)) {
      (memo, nextField) =>
        val next = toSchema(Schema(nextField.getValue, memo._2))
        (nextField.getName.getText -> next.node :: memo._1, next.modelDefinitions)
    }

    val finalFinalDefinitions = definitionId -> obj("type" -> string("object"), "properties" -> obj(finalFields: _*)) :: finalDefinitions
    Schema(obj("$ref" -> string(s"#/definitions/$definitionId")), finalFinalDefinitions)
  }

  def toSchema(input: JsonNode): Schema = toSchema(Schema(input, Nil))
}
