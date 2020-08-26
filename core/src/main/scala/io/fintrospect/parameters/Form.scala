package io.fintrospect.parameters

import io.fintrospect.util.ExtractionError

/**
  * The body entity of a encoded HTML form. Basically a wrapper for Form construction and field extraction.
  */
case class Form protected[parameters](fields: Map[String, Seq[String]] = Map.empty,
                                      files: Map[String, Seq[MultiPartFile]] = Map.empty,
                                      errors: Seq[ExtractionError] = Nil) {

  def isValid = errors.isEmpty

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

  def +(key: String, value: String) = Form(fields + (key -> (value :: fields.getOrElse(key, Seq()).toList)), files, errors)

  def +(key: String, value: MultiPartFile) = Form(fields, files + (key -> (value :: files.getOrElse(key, Seq()).toList)), errors)
}

object Form {

  /**
    * Make a form to send to a downstream system from a set of bindings
    */
  def apply(bindings: Iterable[FormElementBinding]*): Form = bindings.flatten.foldLeft(new Form())((f, b) => b(f))
}
