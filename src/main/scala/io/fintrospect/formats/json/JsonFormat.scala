package io.fintrospect.formats.json

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

  def pretty(in: ROOT_NODETYPE): String

  def compact(in: ROOT_NODETYPE): String

  def obj(fields: Iterable[Field]): ROOT_NODETYPE

  def obj(fields: Field*): ROOT_NODETYPE

  def objSym(fields: (Symbol, NODETYPE)*): ROOT_NODETYPE = obj(fields.map(p => p._1.name -> p._2):_*)

  def array(elements: Iterable[NODETYPE]): NODETYPE

  def array(elements: NODETYPE*): NODETYPE

  def string(value: String): NODETYPE

  def number(value: Int): NODETYPE

  def number(value: BigDecimal): NODETYPE

  def number(value: Long): NODETYPE

  def number(value: BigInteger): NODETYPE

  def boolean(value: Boolean): NODETYPE

  def nullNode(): NODETYPE
}

object JsonFormat {

  class InvalidJson extends Exception
  class InvalidJsonForDecoding extends Exception

}
