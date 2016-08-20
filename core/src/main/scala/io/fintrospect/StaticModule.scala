package io.fintrospect

import com.google.common.io.Resources.toByteArray
import com.twitter.finagle.http.Method.Get
import com.twitter.finagle.http.Status.Ok
import com.twitter.finagle.http.path.{->, Path, Root}
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Filter, Service}
import com.twitter.io.Buf.ByteArray.Owned
import io.fintrospect.ContentType.lookup
import io.fintrospect.Module.ServiceBinding
import io.fintrospect.formats.ResponseBuilder.HttpResponse

object StaticModule {
  /**
    * Module to serve static resources. This defaults to using a Classpath ResourceLoader (to serve from the root of the classpath)
    * @param basePath
    * @param resourceLoader
    * @param moduleFilter
    * @return
    */
  def apply(basePath: Path, resourceLoader: ResourceLoader = ResourceLoader.Classpath("/"), moduleFilter: Filter[Request, Response, Request, Response] = Filter.identity) = {
    new StaticModule(basePath, resourceLoader, moduleFilter)
  }
}

class StaticModule private(basePath: Path, resourceLoader: ResourceLoader, moduleFilter: Filter[Request, Response, Request, Response]) extends Module {

  override protected[fintrospect] def serviceBinding: ServiceBinding = {
    case Get -> path if exists(path) =>
      moduleFilter.andThen(Service.mk[Request, Response] {
        request => HttpResponse(lookup(convertPath(path))).withCode(Ok).withContent(Owned(toByteArray(resourceLoader.load(convertPath(path)))))
      })
  }

  private def exists(path: Path) = if (path.startsWith(basePath)) resourceLoader.load(convertPath(path)) != null else false

  private def convertPath(path: Path) = {
    val newPath = if (basePath == Root) path.toString else path.toString.replace(basePath.toString, "")
    val resolved = if (newPath == "") "/index.html" else newPath
    resolved.replaceFirst("/", "")
  }
}
