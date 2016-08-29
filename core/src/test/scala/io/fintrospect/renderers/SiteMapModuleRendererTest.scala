package io.fintrospect.renderers

import java.net.URL

import com.twitter.finagle.Service
import com.twitter.finagle.http.Method.{Connect, Delete, Get, Head, Options, Post, Put, Trace}
import com.twitter.finagle.http.Status.{BadRequest, NotFound, Ok}
import com.twitter.finagle.http.path.Root
import com.twitter.finagle.http.{Method, Request, Response}
import com.twitter.util.Future
import io.fintrospect.util.HttpRequestResponseUtil.statusAndContentFrom
import io.fintrospect.{NoSecurity, RouteSpec, ServerRoute}
import org.scalatest.{FunSpec, Matchers}

class SiteMapModuleRendererTest extends FunSpec with Matchers {

  it("renders 400") {
    new SiteMapModuleRenderer(new URL("http://fintrospect.io")).badRequest(Nil).status shouldBe BadRequest
  }

  it("renders 404") {
    new SiteMapModuleRenderer(new URL("http://fintrospect.io")).notFound(Request()).status shouldBe NotFound
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
    status shouldBe Ok
    content shouldBe <urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
      <url>
        <loc>
          http://fintrospect.io/bob/GET
        </loc>
      </url>
    </urlset>.toString()
  }

  private def endpointFor(method: Method): ServerRoute[Request, Response] = {
    RouteSpec().at(method) / method.toString() bindTo Service.mk[Request, Response]((r) => Future.value(Response()))
  }
}
