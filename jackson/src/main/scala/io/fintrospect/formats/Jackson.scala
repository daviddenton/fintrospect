package io.fintrospect.formats

import java.math.BigInteger

import com.fasterxml.jackson.databind.DeserializationFeature.{USE_BIG_DECIMAL_FOR_FLOATS, USE_BIG_INTEGER_FOR_INTS}
import com.fasterxml.jackson.databind.node._
import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.twitter.finagle.http.Status
import com.twitter.io.Buf
import io.fintrospect.ResponseSpec
import io.fintrospect.parameters.{BodySpec, ParameterSpec}

import scala.collection.JavaConverters._

/**
  * Jackson support (application/json content type) - uses BigDecimal for decimal
  */
object Jackson extends JsonLibrary[JsonNode, JsonNode] {

  object JsonFormat extends JsonFormat[JsonNode, JsonNode] {
    private val mapper = new ObjectMapper() {
      {
        registerModule(DefaultScalaModule)
        configure(USE_BIG_INTEGER_FOR_INTS, true)
        configure(USE_BIG_DECIMAL_FOR_FLOATS, true)
      }
    }

    override def parse(in: String): JsonNode = mapper.readValue(in, classOf[JsonNode])

    override def pretty(node: JsonNode): String = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(node)

    override def compact(node: JsonNode): String = mapper.writeValueAsString(node)

    override def obj(fields: Iterable[Field]): JsonNode = {
      val root = mapper.createObjectNode()
      root.setAll(Map(fields.toSeq: _*).asJava)
      root
    }

    override def array(elements: Iterable[JsonNode]): ArrayNode = {
      val root = mapper.createArrayNode()
      root.addAll(elements.asJavaCollection)
      root
    }

    override def string(value: String) = new TextNode(value)

    override def number(value: Int) = new IntNode(value)

    override def number(value: BigDecimal) = new DecimalNode(value.bigDecimal)

    override def number(value: Long) = new LongNode(value)

    override def number(value: BigInteger) = new BigIntegerNode(value)

    override def boolean(value: Boolean) = BooleanNode.valueOf(value)

    override def nullNode() = NullNode.instance

    def encodeToBuf[T](in: T): Buf = Buf.ByteArray.Owned(mapper.writeValueAsBytes(in))

    def encode[T](in: T): JsonNode = mapper.convertValue(in, classOf[JsonNode])

    def decode[R](in: JsonNode)(implicit mf: Manifest[R]): R = mapper.readerFor(mf.runtimeClass).readValue(in)
  }

  /**
    * Convenience method for creating BodySpecs that just use straight JSON encoding/decoding logic
    */
  def bodySpec[R](description: Option[String] = None)(implicit mf: Manifest[R]) =
    BodySpec.json(description, JsonFormat).map(j => JsonFormat.decode[R](j), (u: R) => JsonFormat.encode(u))

  /**
    * Convenience method for creating ResponseSpecs that just use straight JSON encoding/decoding logic for examples
    */
  def responseSpec[R](statusAndDescription: (Status, String), example: R)(implicit mf: Manifest[R]) =
    ResponseSpec.json(statusAndDescription, JsonFormat.encode(example.asInstanceOf[AnyRef]), JsonFormat)

  /**
    * Convenience method for creating ParameterSpecs that just use straight JSON encoding/decoding logic
    */
  def parameterSpec[R](name: String, description: Option[String] = None)(implicit mf: Manifest[R]) =
    ParameterSpec.json(name, description.orNull, JsonFormat).map(j => JsonFormat.decode[R](j), (u: R) => JsonFormat.encode(u))
}
