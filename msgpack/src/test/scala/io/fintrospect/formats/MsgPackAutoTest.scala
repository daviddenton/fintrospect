package io.fintrospect.formats

import com.twitter.io.Buf
import io.fintrospect.parameters.BodySpec

import scala.language.reflectiveCalls

class MsgPackAutoTest extends AutoFiltersSpec(MsgPack.Auto) {

  override def toBuf(l: Letter) = MsgPackMsg(l).toBuf

  override def fromBuf(s: Buf): Letter = MsgPack.Format.decode[Letter](s)

  override def bodySpec: BodySpec[Letter] = MsgPack.bodySpec[Letter]()

  override def transform() = MsgPack.Auto.tToToOut[Letter]
}
