package io.fintrospect.templating

import com.twitter.io.Buf
import org.scalatest.{FunSpec, Matchers}

abstract class TemplatesSpec[T](renderers: Templates, subProjectName: String) extends FunSpec with Matchers {

  describe(subProjectName + " templating") {

    describe("caching classpath") {
      val renderer = renderers.CachingClasspath()
      it("renders a template from a case class on the classpath") {
        renderOnClasspath(renderer)
      }

      it("renders a template from a case class with overridden template") {
        renderAtRoot(renderer)
      }
    }

    describe("caching file-based") {
      val renderer = renderers.Caching(subProjectName + "/src/test/resources")
      it("renders a template from a case class on the classpath") {
        renderOnClasspath(renderer)
      }

      it("renders a template from a case class with overridden template") {
        renderAtRoot(renderer)
      }
    }

    describe("hot reload") {
      val renderer = renderers.HotReload(subProjectName + "/src/test/resources")
      it("renders a template from a case class on the classpath") {
        renderOnClasspath(renderer)
      }

      it("renders a template from a case class with overridden template") {
        renderAtRoot(renderer)
      }
    }
  }

  private val items = Seq(
    Item("item1", "£1", Seq(Feature("pretty"))),
    Item("item2", "£3", Seq(Feature("nasty")))
  )

  private def renderOnClasspath(renderer: TemplateRenderer): Unit = {
    Buf.Utf8.unapply(renderer.toBuf(OnClasspath(items))).get shouldBe "Name:item1Price:£1Feature:prettyName:item2Price:£3Feature:nasty"
  }

  private def renderAtRoot(renderer: TemplateRenderer): Unit = {
    Buf.Utf8.unapply(renderer.toBuf(AtRoot(items))).get shouldBe "AtRootName:item1Price:£1Feature:prettyAtRootName:item2Price:£3Feature:nasty"
  }
}
