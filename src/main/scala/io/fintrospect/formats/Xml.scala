package io.fintrospect.formats

import com.twitter.finagle.Service
import com.twitter.finagle.http.Status.Ok
import com.twitter.finagle.http.{Request, Response, Status}
import io.fintrospect.ContentTypes
import io.fintrospect.parameters.Body

import scala.xml.Elem

/**
  * Native (Elem-based) Xml support (application/xml content type)
  */
object Xml {

  /**
    * Auto-marshalling filters which can be used to create Services which take and return domain objects
    * instead of HTTP responses
    */
  object Filters {

    /**
      * Wrap the enclosed service with auto-marshalling of input and output case class instances for HTTP POST scenarios
      * which return an object.
      * HTTP OK is returned by default in the auto-marshalled response (overridable).
      */
    def AutoInOut[BODY, OUT](svc: Service[Elem, Elem], successStatus: Status = Ok): Service[Request, Response] = {
      val body = Body.xml(None)
      import Xml.ResponseBuilder.implicits._
      Service.mk { req: Request => svc(body <-- req).map(successStatus(_)) }
    }
  }

  object ResponseBuilder extends AbstractResponseBuilder[Elem] {

    private def format(node: Elem): String = node.toString()

    private def formatErrorMessage(errorMessage: String): Elem = <message>{errorMessage}</message>

    private def formatError(throwable: Throwable): Elem = formatErrorMessage(Option(throwable.getMessage).getOrElse(throwable.getClass.getName))

    override def HttpResponse() = new ResponseBuilder[Elem](format, formatErrorMessage, formatError, ContentTypes.APPLICATION_XML)
  }

}