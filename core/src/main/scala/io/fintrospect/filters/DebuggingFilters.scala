package io.fintrospect.filters

import com.twitter.finagle.Filter
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.tracing.{Trace, TraceId}

/**
  * A set of handy filters for use during development.
  */
object DebuggingFilters {

  /**
    * Print details of the request before it is sent to the next service.
    */
  val PrintRequest = RequestFilters.Tap {
    req: Request =>
      println(Seq(s"***** REQUEST: ${req.method}: ${req.uri} (trace: ${Trace.id.traceId}) *****",
        "Headers: " + req.headerMap,
        "Params: " + req.params,
        s"Content (${req.length}b):" + req.contentString
      ).mkString("\n"))
  }

  /**
    * Print details of the response before it is returned.
    */
  val PrintResponse = Filter.mk[Request, Response, Request, Response] {
    (req, svc) =>
      svc(req)
        .onSuccess {
          response =>
            println(Seq(s"***** RESPONSE ${response.status.code} to ${req.method}: ${req.uri} (trace: ${Trace.id.traceId}) *****",
              "Headers: " + response.headerMap,
              s"Content (${response.length}b):" + response.contentString
            ).mkString("\n"))
        }
        .onFailure {
          t: Throwable =>
            println(s"***** RESPONSE FAILED to ${req.method}: ${req.uri} (trace: ${Trace.id.traceId}) *****")
            t.printStackTrace()
        }
  }

  /**
    * Print details of a request and it's response.
    */
  val PrintRequestAndResponse = PrintRequest.andThen(PrintResponse)

}
