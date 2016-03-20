package io.fintrospect

import com.google.common.io.Resources.toByteArray
import com.twitter.finagle.http.Method.Get
import com.twitter.finagle.http.Status.Ok
import com.twitter.finagle.http.path.{->, Path, Root}
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Filter, Service}
import com.twitter.io.Buf.ByteArray.Owned
import io.fintrospect.ContentType.lookup
import io.fintrospect.Types.ServiceBinding
import io.fintrospect.formats.ResponseBuilder.{HttpResponse}

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
        request => HttpResponse(lookup(subPath)).withCode(Ok).withContent(Owned(toByteArray(getClass.getResource(subPath))))
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
