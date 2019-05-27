package io.fintrospect.parameters

import java.io.File
import java.nio.charset.StandardCharsets.UTF_8

import com.google.common.io.Files
import com.twitter.io.{Buf, Bufs}
import org.scalatest.{FunSpec, Matchers}

class MultipartFileTest extends FunSpec with Matchers {

  describe("OnDiskMultiPartFile") {
    it("converts toFileElement") {
      val tempFile = File.createTempFile("temp", "file")
      Files.write("hello bob", tempFile, UTF_8)
      tempFile.deleteOnExit()
      Bufs.asUtf8String(OnDiskMultiPartFile("file", tempFile, None).toFileElement("hello").content) shouldBe "hello bob"
    }
  }

  describe("InMemoryMultiPartFile") {
    it("converts toFileElement") {
      Bufs.asUtf8String(InMemoryMultiPartFile("file", Buf.Utf8("hello bob"), None).toFileElement("hello").content) shouldBe "hello bob"
    }
  }

}
