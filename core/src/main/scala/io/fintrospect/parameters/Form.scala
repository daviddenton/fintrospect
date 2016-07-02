package io.fintrospect.parameters

/**
 * The body entity of a encoded HTML form. Basically a wrapper for Form construction and field extraction.
 */
case class Form(private val fields: collection.Map[String, Set[String]]) extends Iterable[(String, Set[String])] {

  /**
   * Convenience method to retrieve multiple fields from form
   */
  def <--[A](fieldA: Retrieval[Form, A]):
  A = fieldA <-- this

  /**
   * Convenience method to retrieve multiple fields from form
   */
  def <--[A, B](fieldA: Retrieval[Form, A],
                fieldB: Retrieval[Form, B]):
  (A, B) = (fieldA <-- this, fieldB <-- this)

  /**
   * Convenience method to retrieve multiple fields from form
   */
  def <--[A, B, C](fieldA: Retrieval[Form, A],
                   fieldB: Retrieval[Form, B],
                   fieldC: Retrieval[Form, C]):
  (A, B, C) = (fieldA <-- this, fieldB <-- this, fieldC <-- this)

  /**
   * Convenience method to retrieve multiple fields from form
   */
  def <--[A, B, C, D](fieldA: Retrieval[Form, A],
                      fieldB: Retrieval[Form, B],
                      fieldC: Retrieval[Form, C],
                      fieldD: Retrieval[Form, D]):
  (A, B, C, D) = (fieldA <-- this, fieldB <-- this, fieldC <-- this, fieldD <-- this)

  /**
   * Convenience method to retrieve multiple fields from form
   */
  def <--[A, B, C, D, E](fieldA: Retrieval[Form, A],
                         fieldB: Retrieval[Form, B],
                         fieldC: Retrieval[Form, C],
                         fieldD: Retrieval[Form, D],
                         fieldE: Retrieval[Form, E]):
  (A, B, C, D, E) = (fieldA <-- this, fieldB <-- this, fieldC <-- this, fieldD <-- this, fieldE <-- this)

  /**
   * Convenience method to retrieve multiple fields from form
   */
  def <--[A, B, C, D, E, F](fieldA: Retrieval[Form, A],
                            fieldB: Retrieval[Form, B],
                            fieldC: Retrieval[Form, C],
                            fieldD: Retrieval[Form, D],
                            fieldE: Retrieval[Form, E],
                            fieldF: Retrieval[Form, F]):
  (A, B, C, D, E, F) = (fieldA <-- this, fieldB <-- this, fieldC <-- this, fieldD <-- this, fieldE <-- this, fieldF <-- this)

  def +(key: String, value: String) = Form(fields + (key -> (fields.getOrElse(key, Set()) + value)))

  def get(name: String): Option[Seq[String]] = fields.get(name).map(_.toSeq)

  override def iterator: Iterator[(String, Set[String])] = fields.iterator
}

object Form {
  def apply(bindings: Iterable[FormFieldBinding]*): Form = bindings.flatten.foldLeft(new Form(Map.empty))((f, b) => b(f))
}