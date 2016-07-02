package io.fintrospect.templating

import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Filter, Service}
import com.twitter.util.{Await, Future}
import io.fintrospect.util.HttpRequestResponseUtil.contentFrom
import org.scalatest.{FunSpec, ShouldMatchers}

abstract class RenderTemplateEngineSpec[T](loaders: TemplateLoaders[T], subProjectName: String) extends FunSpec with ShouldMatchers {

  def renderViewFor(loader: TemplateLoader[T]): Filter[Request, Response, Request, View]

  describe(subProjectName + " templating") {

    describe("caching classpath") {
      val loader = loaders.CachingClasspath()
      it("renders a template from a case class on the classpath") {
        renderOnClasspath(loader)
      }

      it("renders a template from a case class with overridden template") {
        renderAtRoot(loader)
      }
    }

    describe("caching file-based") {
      val loader = loaders.Caching(subProjectName + "/src/test/resources")
      it("renders a template from a case class on the classpath") {
        renderOnClasspath(loader)
      }

      it("renders a template from a case class with overridden template") {
        renderAtRoot(loader)
      }
    }

    describe("hot reload") {
      val loader = loaders.HotReload(subProjectName + "/src/test/resources")
      it("renders a template from a case class on the classpath") {
        renderOnClasspath(loader)
      }

      it("renders a template from a case class with overridden template") {
        renderAtRoot(loader)
      }
    }
  }

  private val items = Seq(
    Item("item1", "£1", Seq(Feature("pretty"))),
    Item("item2", "£3", Seq(Feature("nasty")))
  )

  private def renderOnClasspath(loader: TemplateLoader[T]): Unit = {
    val svc = Service.mk[Request, View]((r) => Future.value(OnClasspath(items)))
    val apply = renderViewFor(loader).apply(Request(), svc)
    contentFrom(Await.result(apply)) shouldBe "Name:item1Price:£1Feature:prettyName:item2Price:£3Feature:nasty"
  }

  private def renderAtRoot(loader: TemplateLoader[T]): Unit = {
    val svc = Service.mk[Request, View]((r) => Future.value(AtRoot(items)))
    val apply = renderViewFor(loader).apply(Request(), svc)
    contentFrom(Await.result(apply)) shouldBe "AtRootName:item1Price:£1Feature:prettyAtRootName:item2Price:£3Feature:nasty"
  }
}
