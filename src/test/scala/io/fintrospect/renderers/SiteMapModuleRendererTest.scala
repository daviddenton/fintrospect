package io.fintrospect.renderers

import java.net.URL

import com.twitter.finagle.Service
import com.twitter.finagle.http.Method.Get
import com.twitter.finagle.http.path.Root
import com.twitter.finagle.http.{Status, Request, Response}
import com.twitter.util.Future
import io.fintrospect.RouteSpec
import io.fintrospect.parameters.NoSecurity
import io.fintrospect.util.HttpRequestResponseUtil._
import org.scalatest.{FunSpec, ShouldMatchers}

class SiteMapModuleRendererTest extends FunSpec with ShouldMatchers {

  it("should describe module as a sitemap") {
    val rsp = new SiteMapModuleRenderer(new URL("http://fintrospect.io")).description(Root / "bob", NoSecurity, Seq(
      RouteSpec().at(Get) / "sue" / "rita" bindTo (() => Service.mk[Request, Response]((r) => Future.value(Response())))
    ))
    val (status, content) = statusAndContentFrom(rsp)
    status shouldBe Status.Ok
    content shouldBe <urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
      <url>
        <loc>
          http://fintrospect.io/bob/sue/rita
        </loc>
      </url>
    </urlset>.toString()
  }
}
