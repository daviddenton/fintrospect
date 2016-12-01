package io.fintrospect.templating

import java.io.{StringReader, StringWriter}
import java.util.concurrent.Callable

import com.github.mustachejava.DefaultMustacheFactory
import org.scalatest.{FunSpec, Matchers}

class ScalaObjectHandlerTest extends FunSpec with Matchers {

  describe("ScalaObjectHandler") {
    it("maps") {
      render("{{#map}}{{test}}{{test2}}{{/map}}", Map("map" -> Map("test" -> "fred"))) shouldBe "fred"
    }

    it("handler") {
      val model = new {
        val list = Seq(new {
          lazy val optionalHello = Some("Hello")
          val futureWorld = new Callable[String] {
            def call(): String = "world"
          }
          val test = true
          val num = 0
        }, new {
          val optionalHello = Some("Goodbye")
          val futureWorld = new Callable[String] {
            def call(): String = "thanks for all the fish"
          }
          lazy val test = false
          val map = Map("value" -> "test")
          val num = 1
        })
      }

      render("{{#list}}{{optionalHello}}, {{futureWorld}}!" +
        "{{#test}}?{{/test}}{{^test}}!{{/test}}{{#num}}?{{/num}}{{^num}}!{{/num}}" +
        "{{#map}}{{value}}{{/map}}\n{{/list}}", model) shouldBe "Hello, world!?!\nGoodbye, thanks for all the fish!!?test\n"
    }

    it("steams") {
      val model = new {
        val stream = Stream(
          new {
            val value = "hello"
          },
          new {
            val value = "world"
          })
      }
      render("{{#stream}}{{value}}{{/stream}}", model) shouldBe "helloworld"
    }

    it("unit") {
      val model = new {
        val test = if (false) "test"
      }
      render("{{test}}", model) shouldBe ""
    }

    it("options") {
      val model = new {
        val foo = Some("Hello")
        val bar = None
      }
      render("{{foo}}{{bar}}", model) shouldBe "Hello"
    }
  }

  private def render(template: String, model: Any): String = {
    val mf = new DefaultMustacheFactory()
    mf.setObjectHandler(new ScalaObjectHandler)
    val m = mf.compile(new StringReader(template), "name")
    val sw = new StringWriter
    m.execute(sw, model).close()
    sw.toString
  }
}
