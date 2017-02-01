package io.fintrospect.parameters

import java.time.format.DateTimeFormatter.{ISO_LOCAL_DATE, ISO_LOCAL_DATE_TIME, ISO_ZONED_DATE_TIME}
import java.time.{LocalDate, LocalDateTime, ZonedDateTime}
import java.util.UUID

import io.fintrospect.formats.{Argo, JsonLibrary}

import scala.xml.{Elem, XML}

/**
  * Spec required to marshal and unmarshal a parameter of a custom type
  *
  * @param deserialize function to take the input string from the request and attempt to construct a deserialized instance of T. Exceptions are
  *                    automatically caught and translated into the appropriate result, so just concentrate on the Happy-path case
  * @param paramType   The parameter type to be used in the documentation. For custom types, this is usually ObjectParamType (for JSON) or StringParamType
  * @param serialize   function to take the input type and serialize it to a string to be represented in the request
  * @tparam T the type of the deserialised parameter
  * @return a parameter for retrieving a value of type [T] from the request
  */
case class ParameterSpec[T](paramType: ParamType,
                            deserialize: String => T,
                            serialize: T => String = (s: T) => s.toString) {

  /**
    * Convenience method for avoiding using map() with AnyVal case-classes. Due to the Scala type system,
    * this method provides no compile-time safety as to if the signatures are compatible (ie. it won't complain at
    * ParameterSpec.int().as[MyStringAnyVal]. However, obviously a runtime exception will be generated, since this method
    * use reflection to get the value of the underlying case-class value
    * @tparam Wrapper - the value type of the case class AnyVal
    */
  def as[Wrapper <: AnyVal with Product](implicit mf: Manifest[Wrapper]): ParameterSpec[Wrapper] = {
    val ctr = mf.runtimeClass.getConstructors.iterator.next()
    val field = mf.runtimeClass.getDeclaredFields.iterator.next()
    field.setAccessible(true)
    map((t: T) => { ctr.newInstance(t.asInstanceOf[Object]).asInstanceOf[Wrapper]},
      (wrapper: Wrapper) => field.get(wrapper).asInstanceOf[T]
    )
  }

  /**
    * Bi-directional map functions for this ParameterSpec type. Use this to implement custom Parameter types
    */
  def map[O](in: T => O, out: O => T): ParameterSpec[O] = ParameterSpec[O](paramType, s => in(deserialize(s)), b => serialize(out(b)))

  /**
    * Uni-directional map functions for this ParameterSpec type. Use this to implement custom Parameter types
    */
  def map[O](in: T => O): ParameterSpec[O] = ParameterSpec[O](paramType, s => in(deserialize(s)))
}

/**
  * Predefined ParameterSpec instances for common types. These are mappable to custom types, so start with these.
  */
object ParameterSpec {

  def localDate() = string().map(LocalDate.parse, (i: LocalDate) => ISO_LOCAL_DATE.format(i))

  def zonedDateTime() = string().map(ZonedDateTime.parse, (i: ZonedDateTime) => ISO_ZONED_DATE_TIME.format(i))

  def dateTime() = string().map(LocalDateTime.parse, (i: LocalDateTime) => ISO_LOCAL_DATE_TIME.format(i))

  def boolean() = ParameterSpec[Boolean](BooleanParamType, _.toBoolean, _.toString)

  def string(validation: StringValidations.Rule = StringValidations.EmptyIsInvalid) = ParameterSpec[String](StringParamType, validation, _.toString)

  def uuid() = string().map(UUID.fromString)

  def bigDecimal() = ParameterSpec[BigDecimal](NumberParamType, BigDecimal(_), _.toString())

  def long() = ParameterSpec[Long](IntegerParamType, _.toLong, _.toString)

  def int() = ParameterSpec[Int](IntegerParamType, _.toInt, _.toString)

  def integer() = ParameterSpec[Integer](IntegerParamType, new Integer(_), _.toString)

  def json[T](jsonLib: JsonLibrary[T, _] = Argo) = ParameterSpec[T](ObjectParamType, jsonLib.JsonFormat.parse, jsonLib.JsonFormat.compact)

  def xml() = ParameterSpec[Elem](StringParamType, XML.loadString, _.toString())
}
