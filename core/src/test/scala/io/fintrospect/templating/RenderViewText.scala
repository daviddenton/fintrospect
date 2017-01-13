package io.fintrospect.templating

import com.twitter.finagle.Service
import com.twitter.finagle.http.{Request, Status}
import com.twitter.io.{Buf, Bufs}
import com.twitter.util.Await.result
import io.fintrospect.formats.Html
import io.fintrospect.templating.RenderView.Redirect
import org.scalatest.{FunSpec, Matchers}

class RenderViewText extends FunSpec with Matchers {

  describe("RenderView") {
    val renderView = new RenderView(Html.ResponseBuilder, new TemplateRenderer() {
      override def toBuf(view: View): Buf = Bufs.utf8Buf(view.template)
    })

    it("creates a standard View") {
      val response = result(renderView(Request(), Service.const(OnClasspath(Nil))))
      response.status shouldBe Status.Ok
      response.contentString shouldBe "io/fintrospect/templating/OnClasspath"
    }

    it("creates redirect when passed a RenderView.Redirect") {
      val response = result(renderView(Request(), Service.const(Redirect("newLocation", Status.BadGateway))))
      response.status shouldBe Status.BadGateway
      response.headerMap("Location") shouldBe "newLocation"
    }
  }

}
