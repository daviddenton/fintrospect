package io.github.daviddenton.fintrospect

import java.util.{List => JList, Map => JMap}

import com.twitter.finagle.http.{MediaType, Method}
import com.twitter.io.Charsets
import io.github.daviddenton.fintrospect.FinagleTypeAliases._
import org.jboss.netty.handler.codec.http.{HttpHeaders, QueryStringDecoder}

import scala.util.Try

/**
 * Copied from com.twitter.finagle.http.RequestParamMap in order to provide support for our Request type
 * @param request
 */
class RequestParams(val request: FTRequest) {

  private val getParams = parseParams(request.getUri)

  private val postParams = {
    if (request.getMethod != Method.Trace &&
      mediaType == Some(MediaType.WwwForm) &&
      request.getContent.readableBytes > 0) {
      parseParams("?" + request.getContent.toString(Charsets.Utf8))
    } else {
      new java.util.HashMap[String, JList[String]]
    }
  }

  def get(name: String) =
    jget(postParams, name) match {
      case None => jget(getParams, name)
      case value => value
    }

  private def mediaType: Option[String] =
    Option(request.headers().get(HttpHeaders.Names.CONTENT_TYPE)).flatMap { contentType =>
      val beforeSemi =
        contentType.indexOf(";") match {
          case -1 => contentType
          case n => contentType.substring(0, n)
        }
      val mediaType = beforeSemi.trim
      if (mediaType.nonEmpty) Some(mediaType.toLowerCase) else None
    }

  private def parseParams(s: String) = {
    Try(new QueryStringDecoder(s).getParameters).toOption.getOrElse(new java.util.HashMap[String, JList[String]])
  }

  private def jget(params: JMap[String, JList[String]], name: String) = Option(params.get(name)).map(_.get(0))
}
