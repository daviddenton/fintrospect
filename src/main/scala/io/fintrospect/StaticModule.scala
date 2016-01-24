package io.fintrospect

import com.google.common.io.Resources._
import com.twitter.finagle.http.Method.Get
import com.twitter.finagle.http.Status._
import com.twitter.finagle.http.path._
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Filter, Service}
import com.twitter.io.Buf.ByteArray._
import io.fintrospect.Types._
import io.fintrospect.formats.ResponseBuilder._

object StaticModule {
  def apply(basePath: Path, baseDir: String = "/", moduleFilter: Filter[Request, Response, Request, Response] = Filter.identity) = {
    val withStarting = if (baseDir.startsWith("/")) baseDir else "/" + baseDir
    val withEnding = if (withStarting.endsWith("/")) withStarting else withStarting + "/"
    new StaticModule(basePath, withEnding, moduleFilter)
  }
}

class StaticModule private(basePath: Path, baseDir: String, moduleFilter: Filter[Request, Response, Request, Response]) extends Module {

  override protected def serviceBinding: ServiceBinding = {
    case method -> path if method == Get && exists(path) =>
      moduleFilter.andThen(Service.mk[Request, Response] {
        val subPath = baseDir + path.toString.replace(basePath.toString + "/", "")
        request => HttpResponse(ContentType.lookup(subPath)).withCode(Ok).withContent(Owned(toByteArray(getClass.getResource(subPath))))
      })
  }

  private def exists(path: Path): Boolean = {
    if (path.startsWith(basePath)) {
      getClass.getResource(baseDir + path.toString.replace(basePath.toString + "/", "")) != null
    } else false
  }
}
