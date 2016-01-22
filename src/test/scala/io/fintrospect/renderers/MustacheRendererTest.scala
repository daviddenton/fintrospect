package io.fintrospect.renderers

import com.twitter.util.Await._
import io.fintrospect.formats.Html
import io.fintrospect.util.HttpRequestResponseUtil.contentFrom
import org.scalatest.{FunSpec, ShouldMatchers}

case class Item(name: String, price: String, features: Seq[Feature])

case class Feature(description: String)

case class OnClasspath(items: Seq[Item]) extends View

case class AtRoot(items: Seq[Item]) extends View {
  override val template = "AtRootBob"
}

class MustacheRendererTest extends FunSpec with ShouldMatchers {

  private val items = Seq(
    Item("item1", "£1", Seq(Feature("pretty"))),
    Item("item2", "£3", Seq(Feature("nasty")))
  )

  it("renders a mustache template from a case class on the classpath") {
    contentFrom(result(
      new MustacheRenderer(Html.ResponseBuilder)
        .apply(OnClasspath(items)))) shouldBe "Name:item1Price:£1Feature:prettyName:item2Price:£3Feature:nasty"
  }

  it("renders a mustache template from a case class with overridden template") {
    contentFrom(result(
      new MustacheRenderer(Html.ResponseBuilder)
        .apply(AtRoot(items)))) shouldBe "AtRootName:item1Price:£1Feature:prettyAtRootName:item2Price:£3Feature:nasty"
  }
}
