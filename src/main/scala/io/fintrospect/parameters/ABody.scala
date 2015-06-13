package io.fintrospect.parameters

import io.fintrospect.{ContentType, ContentTypes}

case class AForm private(private val fields: List[Parameter[_]]) extends ABody {
  override def iterator: Iterator[Parameter[_]] = fields.iterator

  def withField(field: Parameter[_]) = copy(field :: fields)

  override val contentType: ContentType = ContentTypes.APPLICATION_FORM_URLENCODED
}

object JsonBody extends ABody {
  override val contentType: ContentType = ContentTypes.APPLICATION_JSON

  override def iterator: Iterator[Parameter[_]] = Nil.iterator
}

trait ABody extends Iterable[Parameter[_]] {
  val contentType: ContentType
}