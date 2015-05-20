package io.github.daviddenton.fintrospect.parameters

import com.twitter.finagle.http.{MediaType, Request}
import org.jboss.netty.handler.codec.http.QueryStringEncoder

import scala.util.Try

class RequiredFormTest extends JsonSupportingParametersTest[RequiredRequestParameter](Form.required) {
  override def from[X](method: (String, String) => RequiredRequestParameter[X], value: Option[String]): Option[X] = {
    val request = Request()
    request.setContentType(MediaType.WwwForm)
    value.foreach({
      v =>
        request.setContentString(new QueryStringEncoder("") {
          {
            addParam(paramName, v)
          }
        }.toUri.getRawQuery)
    })
    Try(method(paramName, null).from(request)).toOption
  }
}
