package io.fintrospect.templating

import java.io.File

import com.twitter.finagle.Service
import com.twitter.finagle.http.Request
import com.twitter.util.{Await, Future}
import io.fintrospect.formats.Html
import io.fintrospect.util.HttpRequestResponseUtil.contentFrom
import org.scalatest.{FunSpec, ShouldMatchers}

import scala.io.Source

class MustacheTemplateLoaderTest extends FunSpec with ShouldMatchers {

  private val items = Seq(
    Item("item1", "£1", Seq(Feature("pretty"))),
    Item("item2", "£3", Seq(Feature("nasty")))
  )

  describe("CachingClasspath") {
    it("renders a mustache template from a case class on the classpath") {
      val svc = Service.mk[Request, View]((r) => Future.value(OnClasspath(items)))
      val apply = new RenderMustacheView(Html.ResponseBuilder, MustacheTemplateLoader.CachingClasspath()).apply(Request(), svc)
      contentFrom(Await.result(apply)) shouldBe "Name:item1Price:£1Feature:prettyName:item2Price:£3Feature:nasty"
    }

    it("renders a mustache template from a case class with overridden template") {
      val svc = Service.mk[Request, View]((r) => Future.value(AtRoot(items)))
      val apply = new RenderMustacheView(Html.ResponseBuilder, MustacheTemplateLoader.CachingClasspath()).apply(Request(), svc)
      contentFrom(Await.result(apply)) shouldBe "AtRootName:item1Price:£1Feature:prettyAtRootName:item2Price:£3Feature:nasty"
    }
  }

  describe("Caching") {
    it("renders a mustache template from a case class on the classpath") {
      val svc = Service.mk[Request, View]((r) => Future.value(OnClasspath(items)))
      val apply = new RenderMustacheView(Html.ResponseBuilder, MustacheTemplateLoader.Caching("mustache/src/test/resources")).apply(Request(), svc)
      contentFrom(Await.result(apply)) shouldBe "Name:item1Price:£1Feature:prettyName:item2Price:£3Feature:nasty"
    }

    it("renders a mustache template from a case class with overridden template") {
      val svc = Service.mk[Request, View]((r) => Future.value(AtRoot(items)))
      val apply = new RenderMustacheView(Html.ResponseBuilder, MustacheTemplateLoader.Caching("mustache/src/test/resources")).apply(Request(), svc)
      contentFrom(Await.result(apply)) shouldBe "AtRootName:item1Price:£1Feature:prettyAtRootName:item2Price:£3Feature:nasty"
    }
  }

  describe("HotReload") {
    it("renders a mustache template from a case class on the classpath") {
      val file = new File("bob.mustache")
      Source.fromString("foo")
      val svc = Service.mk[Request, View]((r) => Future.value(OnClasspath(items)))
      val apply = new RenderMustacheView(Html.ResponseBuilder, MustacheTemplateLoader.HotReload("mustache/src/test/resources")).apply(Request(), svc)
      contentFrom(Await.result(apply)) shouldBe "Name:item1Price:£1Feature:prettyName:item2Price:£3Feature:nasty"
    }

    it("renders a mustache template from a case class with overridden template") {
      val svc = Service.mk[Request, View]((r) => Future.value(AtRoot(items)))
      val apply = new RenderMustacheView(Html.ResponseBuilder, MustacheTemplateLoader.HotReload("mustache/src/test/resources")).apply(Request(), svc)
      contentFrom(Await.result(apply)) shouldBe "AtRootName:item1Price:£1Feature:prettyAtRootName:item2Price:£3Feature:nasty"
    }
  }

}
