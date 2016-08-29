package io.fintrospect.util

import java.time.Instant

import com.twitter.finagle.http.{Request, Status}
import com.twitter.util.Await
import org.scalatest.{FunSpec, Matchers}

class HeapDumpTest extends FunSpec with Matchers {

  describe("HeapDump") {
    it("creates the correct heapdump file") {
      val clock = TestClocks.fixed(Instant.ofEpochMilli(0))
      val response = Await.result(new HeapDump("bob", clock).apply(Request()))
      response.status shouldBe Status.Ok
      response.headerMap("Content-disposition").startsWith("inline; filename=\"heapdump-bob-1970-01-01") shouldBe true
      response.contentType shouldBe Some("application/x-heap-dump;charset=utf-8")
    }
  }
}
