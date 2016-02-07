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
        val subPath = convertPath(path)
        request => HttpResponse(ContentType.lookup(subPath)).withCode(Ok).withContent(Owned(toByteArray(getClass.getResource(subPath))))
      })
  }

  private def exists(path: Path): Boolean = {
    if (path != Root && path.startsWith(basePath)) {
      getClass.getResource(convertPath(path)) != null
    } else false
  }

  private def convertPath(path: Path): String = {
    val newPath = if (basePath == Root) path.toString else path.toString.replace(basePath.toString, "")
    baseDir + newPath.replaceFirst("/", "")
  }
}
