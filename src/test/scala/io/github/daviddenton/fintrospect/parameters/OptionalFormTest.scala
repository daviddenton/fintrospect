package io.github.daviddenton.fintrospect.parameters

import com.twitter.finagle.http.{MediaType, Request}
import org.jboss.netty.handler.codec.http.QueryStringEncoder

class OptionalFormTest extends JsonSupportingParametersTest[OptionalRequestParameter](Form.optional) {
  override def from[X](method: (String, String) => OptionalRequestParameter[X], value: Option[String]): Option[X] = {
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
    method(paramName, null).from(request)
  }
}
