package examples.json4s.extended

import io.fintrospect.parameters.{ParameterSpec, StringParamType}
import org.json4s.JValue

case class Email(to: EmailAddress, from: EmailAddress, subject: String, read: Boolean)

object Email {
  val Spec = ParameterSpec[EmailAddress]("address", Option("user email"), StringParamType, EmailAddress, e => e.address)

  def encodeJson(src: AnyRef): JValue = {
    import org.json4s.native.Serialization
    import org.json4s.{Extraction, NoTypeHints}
    Extraction.decompose(src)(Serialization.formats(NoTypeHints))
  }
}