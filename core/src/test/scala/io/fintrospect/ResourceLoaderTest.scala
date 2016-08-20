package io.fintrospect

import org.scalatest.{FunSpec, ShouldMatchers}

import scala.io.Source

class ResourceLoaderTest extends FunSpec with ShouldMatchers {

  describe("Classpath loader") {
    val loader = ResourceLoader.Classpath("/")
    describe("for an existing file") {
      it("looks up contents") {
        Source.fromURL(loader.load("mybob.xml")).mkString shouldBe "<xml>content</xml>"
      }
      it("looks up contents of a child file") {
        Source.fromURL(loader.load("io/index.html")).mkString shouldBe "hello from the io index.html"
      }
    }
    describe("for a missing file") {
      it("URL is null") {
        loader.load("notafile") shouldBe null
      }
    }
  }

  describe("Directory loader") {
    val loader = ResourceLoader.Directory("./core/src/test/resources")
    describe("for an existing file") {
      it("looks up contents") {
        Source.fromURL(loader.load("mybob.xml")).mkString shouldBe "<xml>content</xml>"
      }
      it("looks up contents of a child file") {
        Source.fromURL(loader.load("io/index.html")).mkString shouldBe "hello from the io index.html"
      }
    }
    describe("for a missing file") {
      it("URL is null") {
        loader.load("notafile") shouldBe null
      }
    }
  }
}
