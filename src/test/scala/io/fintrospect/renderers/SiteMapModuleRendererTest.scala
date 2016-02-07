package io.fintrospect.renderers

import java.net.URL

import com.twitter.finagle.Service
import com.twitter.finagle.http.Method._
import com.twitter.finagle.http.path.Root
import com.twitter.finagle.http.{Method, Request, Response, Status}
import com.twitter.util.Future
import io.fintrospect.parameters.NoSecurity
import io.fintrospect.util.HttpRequestResponseUtil._
import io.fintrospect.{RouteSpec, ServerRoute}
import org.scalatest.{FunSpec, ShouldMatchers}

class SiteMapModuleRendererTest extends FunSpec with ShouldMatchers {

  it("renders 404") {
    new SiteMapModuleRenderer(new URL("http://fintrospect.io")).badRequest(Seq())
  }

  it("should describe only GET endpoints of module as a sitemap") {
    val rsp = new SiteMapModuleRenderer(new URL("http://fintrospect.io")).description(Root / "bob", NoSecurity, Seq(
      endpointFor(Get),
      endpointFor(Post),
      endpointFor(Delete),
      endpointFor(Put),
      endpointFor(Options),
      endpointFor(Connect),
      endpointFor(Head),
      endpointFor(Trace)
    ))

    val (status, content) = statusAndContentFrom(rsp)
    status shouldBe Status.Ok
    content shouldBe <urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
      <url>
        <loc>
          http://fintrospect.io/bob/GET
        </loc>
      </url>
    </urlset>.toString()
  }

  private def endpointFor(method: Method): ServerRoute[Response] = {
    RouteSpec().at(method) / method.toString() bindTo (() => Service.mk[Request, Response]((r) => Future.value(Response())))
  }
}
