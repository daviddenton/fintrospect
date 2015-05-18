package io.github.daviddenton.fintrospect.parameters

import com.twitter.finagle.http.{MediaType, Request}
import org.jboss.netty.handler.codec.http.QueryStringEncoder

class OptionalFormTest extends JsonSupportingParametersTest[OptionalRequestParameter](Form.optional) {
  override def from[X](param: OptionalRequestParameter[X], value: String): Option[X] = {
    val request = Request()
    request.setContentType(MediaType.WwwForm)
    request.setContentString(new QueryStringEncoder("") {
      {
        addParam(paramName, value)
      }
    }.toUri.getRawQuery)
    param.from(request)
  }
}
