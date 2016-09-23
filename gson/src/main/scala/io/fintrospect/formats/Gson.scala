package io.fintrospect.formats

import java.math.BigInteger

import com.google.gson.{GsonBuilder, JsonArray, JsonElement, JsonNull, JsonObject, JsonParser, JsonPrimitive}

/**
  * GSON JSON support (application/json content type)
  */
object Gson extends JsonLibrary[JsonElement, JsonElement] {

  object JsonFormat extends JsonFormat[JsonElement, JsonElement] {

    private val pretty = new GsonBuilder().serializeNulls().setPrettyPrinting().create()
    private val compact = new GsonBuilder().serializeNulls().create()

    override def parse(in: String) = new JsonParser().parse(in).getAsJsonObject

    override def pretty(node: JsonElement): String = pretty.toJson(node)

    override def compact(node: JsonElement): String = compact.toJson(node)

    override def obj(fields: Iterable[Field]) =
      fields.foldLeft(new JsonObject()) { case (memo, (name, o)) => memo.add(name, o); memo }

    override def obj(fields: Field*) = obj(fields)

    override def array(elements: Iterable[JsonElement]) =
      elements.foldLeft(new JsonArray()) { case (memo, o) => memo.add(o); memo }

    override def array(elements: JsonElement*) = array(elements)

    override def string(value: String) = new JsonPrimitive(value)

    override def number(value: Int) = new JsonPrimitive(value)

    override def number(value: BigDecimal) = new JsonPrimitive(value)

    override def number(value: Long) = new JsonPrimitive(value)

    override def number(value: BigInteger) = new JsonPrimitive(value)

    override def boolean(value: Boolean) = new JsonPrimitive(value)

    override def nullNode() = JsonNull.INSTANCE
  }

}
