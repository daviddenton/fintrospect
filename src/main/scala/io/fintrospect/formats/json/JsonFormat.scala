package io.fintrospect.formats.json

import java.math.BigInteger

/**
 * Capability to create and parse JSON message formats in a generic way.
 */
trait JsonFormat[ROOT_NODETYPE, NODETYPE] {
  type Field = (String, NODETYPE)

  /**
   * Attempt to parse the JSON into the root node type. At the moment, this should throw an exception
   * if the parsing fails. Tempted to make this an Either instead and to avoid the throw.
   */
  def parse(in: String): ROOT_NODETYPE

  def pretty(in: ROOT_NODETYPE): String

  def compact(in: ROOT_NODETYPE): String

  def obj(fields: Iterable[Field]): ROOT_NODETYPE

  def obj(fields: Field*): ROOT_NODETYPE

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
