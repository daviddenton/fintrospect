package io.github.daviddenton.fintrospect.renderers

import com.twitter.finagle.http.path.Path
import io.github.daviddenton.fintrospect.parameters.RequestParameter
import io.github.daviddenton.fintrospect.util.ResponseBuilder
import io.github.daviddenton.fintrospect.{DescriptionRenderer, Route}
import org.jboss.netty.handler.codec.http.HttpResponse
import org.jboss.netty.handler.codec.http.HttpResponseStatus._

import scala.language.implicitConversions

class TypedResponseBuilder[T](newBuilder: () => ResponseBuilder[T],
                              descriptionRenderer: DescriptionRenderer[T] ,
                              bpToError: List[RequestParameter[_]] => T) {

  def apply(): ResponseBuilder[T] = newBuilder()

  def BadRequest(badParameters: List[RequestParameter[_]]): HttpResponse = apply().withCode(BAD_REQUEST).withContent(bpToError(badParameters)).build

  def Description(basePath: Path, routes: Seq[Route]) =  apply().withCode(OK).withContent(descriptionRenderer(basePath, routes)).build
}


