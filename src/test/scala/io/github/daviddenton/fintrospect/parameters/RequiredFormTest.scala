package io.github.daviddenton.fintrospect.parameters

import com.twitter.finagle.http.{MediaType, Request}
import org.jboss.netty.handler.codec.http.QueryStringEncoder

class RequiredFormTest extends JsonSupportingParametersTest[RequiredRequestParameter](Form.required) {
  override def from[X](param: RequiredRequestParameter[X], value: String): Option[X] = {
    val request = Request()
    request.setContentType(MediaType.WwwForm)
    request.setContentString(new QueryStringEncoder("") {
      {
        addParam(paramName, value)
      }
    }.toUri.getRawQuery)
    scala.util.Try(param.from(request)).toOption
  }
}