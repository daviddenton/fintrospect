package io.fintrospect.formats

import java.math.BigInteger

/**
  * Capability to create and parse JSON message formats in a generic way. Used to serialise and deserialise
  * request parameters and bodies.
  */
trait JsonFormat[ROOT_NODETYPE, NODETYPE] {

  type Field = (String, NODETYPE)

  /**
    * Attempt to parse the JSON into the root node type. Implementations should throw an exception
    * if the parsing fails, which is dealt with by the surrounding deserialisation mechanism, so you
    * don't need to worry about having to muddy your own code with the exception handling.
    */
  def parse(in: String): ROOT_NODETYPE

  /**
    * Pretty printed JSON
    */
  def pretty(in: ROOT_NODETYPE): String

  /**
    * Compact printed JSON
    */
  def compact(in: ROOT_NODETYPE): String

  /**
    * Create a JSON object from the passed String -> Node pairs
    */
  def obj(fields: Iterable[Field]): ROOT_NODETYPE

  /**
    * Create a JSON object from the passed String -> Node pairs
    */
  def obj(fields: Field*): ROOT_NODETYPE

  /**
    * Create a JSON object from the passed Symbol -> Node pairs
    */
  def objSym(fields: (Symbol, NODETYPE)*): ROOT_NODETYPE = obj(fields.map(p => p._1.name -> p._2): _*)

  /**
    * Create a JSON array from the passed elements
    */
  def array(elements: Iterable[NODETYPE]): NODETYPE

  /**
    * Create a JSON array from the passed elements
    */
  def array(elements: NODETYPE*): NODETYPE

  /**
    * Create a JSON string node
    */
  def string(value: String): NODETYPE

  /**
    * Create a JSON number node
    */
  def number(value: Int): NODETYPE

  /**
    * Create a JSON number node
    */
  def number(value: BigDecimal): NODETYPE

  /**
    * Create a JSON number node
    */
  def number(value: Long): NODETYPE

  /**
    * Create a JSON number node
    */
  def number(value: BigInteger): NODETYPE

  /**
    * Create a JSON boolean node
    */
  def boolean(value: Boolean): NODETYPE

  /**
    * Create a null JSON node
    */
  def nullNode(): NODETYPE
}

object JsonFormat {

  /**
    * Thrown when an invalid string is passed for conversion to JSON
    */
  class InvalidJson extends Exception

  /**
    * Thrown when an JSON node cannot be auto-decoded to a particular type
    */
  class InvalidJsonForDecoding extends Exception

}
