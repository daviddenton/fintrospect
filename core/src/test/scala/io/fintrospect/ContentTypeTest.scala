package io.fintrospect

import org.scalatest.{FunSpec, Matchers}



class ContentTypeTest extends FunSpec with Matchers {

  it("can guess common content types for a file") {
    ContentType.lookup("foo/bob.html") shouldBe ContentTypes.TEXT_HTML
    ContentType.lookup("bob.js") shouldBe ContentType("application/javascript")
    ContentType.lookup("bob.txt") shouldBe ContentTypes.TEXT_PLAIN
    ContentType.lookup("bob.css") shouldBe ContentType("text/css")
    ContentType.lookup("bob.xml") shouldBe ContentTypes.APPLICATION_XML
    ContentType.lookup("bob.csv") shouldBe ContentType("text/csv")
    ContentType.lookup("bob.jpg") shouldBe ContentType("image/jpeg")
    ContentType.lookup("bob.svg") shouldBe ContentType("image/svg+xml")
    ContentType.lookup("bob.bmp") shouldBe ContentType("image/bmp")
    ContentType.lookup("bob.png") shouldBe ContentType("image/png")
  }

  it("defaults to octet-stream") {
    ContentType.lookup("bob.foo") shouldBe ContentType("application/octet-stream")
  }
}
