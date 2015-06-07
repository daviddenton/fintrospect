package io.fintrospect.parameters

import com.twitter.finagle.http.{MediaType, Request}
import org.jboss.netty.handler.codec.http.QueryStringEncoder

class OptionalFormTest extends JsonSupportingParametersTest[RequestParameter, Optional](Form.optional) {
  override def from[X](method: (String, String) => RequestParameter[X] with Optional[X], value: Option[String]): Option[X] = {
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
