package io.fintrospect.formats

import com.twitter.io.{Buf, Bufs}
import io.fintrospect.parameters.BodySpec

import scala.xml.{Elem, XML}

class XmlAutoTest extends AutoSpec(Xml.Auto) {
  override def toBuf(l: Letter): Buf = Bufs.utf8Buf(transform()(l).toString())

  override def transform(): (Letter => Elem) = (letter: Letter) => <letter>
    <from>{letter.from.address}</from>
    <to>{letter.to.address}</to>
    <message>{letter.message}</message>
  </letter>

  override def fromBuf(s: Buf): Letter = from(XML.loadString(Bufs.asUtf8String(s)))

  private def from(x: Elem) =  Letter(
    StreetAddress((x \ "to").head.text),
    StreetAddress((x \ "from").head.text),
    (x \ "message").head.text
  )

  override def bodySpec: BodySpec[Letter] = BodySpec.xml().map[Letter](from(_))
}

class XmlResponseBuilderTest extends ResponseBuilderSpec(Xml.ResponseBuilder) {
  override val customError = <message>{message}</message>
  override val customErrorSerialized = Bufs.utf8Buf(s"<message>$message</message>")
  override val customType = <okThing>theMessage</okThing>
  override val customTypeSerialized = Bufs.utf8Buf(customType.toString())
}



