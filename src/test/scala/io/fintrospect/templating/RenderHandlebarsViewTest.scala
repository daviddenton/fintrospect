package io.fintrospect.templating

import com.twitter.finagle.Service
import com.twitter.finagle.http.Request
import com.twitter.util.{Await, Future}
import io.fintrospect.formats.Html
import io.fintrospect.templating.{View, RenderHandlebarsView}
import io.fintrospect.util.HttpRequestResponseUtil.contentFrom
import org.scalatest.{FunSpec, ShouldMatchers}


class RenderHandlebarsViewTest extends FunSpec with ShouldMatchers {

  private val items = Seq(
    Item("item1", "£1", Seq(Feature("pretty"))),
    Item("item2", "£3", Seq(Feature("nasty")))
  )

  it("renders a handlebars template from a case class on the classpath") {
    val svc = Service.mk[Request, View]((r) => Future.value(OnClasspath(items)))
    val apply = new RenderHandlebarsView(Html.ResponseBuilder).apply(Request(), svc)
    contentFrom(Await.result(apply)) shouldBe "Name:item1Price:£1Feature:prettyName:item2Price:£3Feature:nasty"
  }

  it("renders a handlebars template from a case class with overridden template") {
    val svc = Service.mk[Request, View]((r) => Future.value(AtRoot(items)))
    val apply = new RenderHandlebarsView(Html.ResponseBuilder).apply(Request(), svc)
    contentFrom(Await.result(apply)) shouldBe "AtRootName:item1Price:£1Feature:prettyAtRootName:item2Price:£3Feature:nasty"
  }
}
