package io.fintrospect.parameters

case class Form(private val fields: collection.Map[String, Set[String]]) extends Iterable[(String, Set[String])] {

  /**
   * Convenience method to retrieve multiple fields from form
   */
  def <--[A](fieldA: Retrieval[A, Form]):
  A = fieldA <-- this

  /**
   * Convenience method to retrieve multiple fields from form
   */
  def <--[A, B](fieldA: Retrieval[A, Form],
                fieldB: Retrieval[B, Form]):
  (A, B) = (fieldA <-- this, fieldB <-- this)

  /**
   * Convenience method to retrieve multiple fields from form
   */
  def <--[A, B, C](fieldA: Retrieval[A, Form],
                   fieldB: Retrieval[B, Form],
                   fieldC: Retrieval[C, Form]):
  (A, B, C) = (fieldA <-- this, fieldB <-- this, fieldC <-- this)

  /**
   * Convenience method to retrieve multiple fields from form
   */
  def <--[A, B, C, D](fieldA: Retrieval[A, Form],
                      fieldB: Retrieval[B, Form],
                      fieldC: Retrieval[C, Form],
                      fieldD: Retrieval[D, Form]):
  (A, B, C, D) = (fieldA <-- this, fieldB <-- this, fieldC <-- this, fieldD <-- this)

  /**
   * Convenience method to retrieve multiple fields from form
   */
  def <--[A, B, C, D, E](fieldA: Retrieval[A, Form],
                         fieldB: Retrieval[B, Form],
                         fieldC: Retrieval[C, Form],
                         fieldD: Retrieval[D, Form],
                         fieldE: Retrieval[E, Form]):
  (A, B, C, D, E) = (fieldA <-- this, fieldB <-- this, fieldC <-- this, fieldD <-- this, fieldE <-- this)

  /**
   * Convenience method to retrieve multiple fields from form
   */
  def <--[A, B, C, D, E, F](fieldA: Retrieval[A, Form],
                            fieldB: Retrieval[B, Form],
                            fieldC: Retrieval[C, Form],
                            fieldD: Retrieval[D, Form],
                            fieldE: Retrieval[E, Form],
                            fieldF: Retrieval[F, Form]):
  (A, B, C, D, E, F) = (fieldA <-- this, fieldB <-- this, fieldC <-- this, fieldD <-- this, fieldE <-- this, fieldF <-- this)

  def +(key: String, value: String) = Form(fields + (key -> (fields.getOrElse(key, Set()) + value)))

  def get(name: String): Option[Seq[String]] = fields.get(name).map(_.toSeq)

  override def iterator: Iterator[(String, Set[String])] = fields.iterator
}

object Form {
  def apply(bindings: Iterable[FormFieldBinding]*): Form = bindings.flatten.foldLeft(new Form(Map.empty))((f, b) => b(f))
}