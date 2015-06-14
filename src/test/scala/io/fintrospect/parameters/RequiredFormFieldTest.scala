package io.fintrospect.parameters

import com.twitter.finagle.http.{MediaType, Request}
import org.jboss.netty.handler.codec.http.QueryStringEncoder

import scala.util.Try

class RequiredFormFieldTest extends JsonSupportingParametersTest[FormField, Mandatory](FormField.required) {

  override def to[X](method: (String, String) => FormField[X] with Mandatory[X], value: X): ParamBinding[X] = method(paramName, null) -> value

  override def from[X](method: (String, String) => FormField[X] with Mandatory[X], value: Option[String])= {
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
