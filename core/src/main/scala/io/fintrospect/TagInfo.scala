package io.fintrospect

/**
  * Info about a Swagger tag.
  */
case class TagInfo(name: String, description: Option[String] = None)

object TagInfo {
  def apply(name: String, description: String): TagInfo =
    TagInfo(name, Some(description))
}
