package io.fintrospect.filters

import com.twitter.finagle.http.{Request, Response}

/**
  * A set of handy filters for use during development.
  */
object DebuggingFilters {

  /**
    * Print details of the request before it is sent to the next service.
    */
  val PrintRequest = RequestFilters.Tap {
    req: Request =>
      println(Seq(s"***** REQUEST: ${req.method}: ${req.uri} *****",
        "Headers: " + req.headerMap,
        "Params: " + req.params,
        s"Content (${req.length}b):" + req.contentString
      ).mkString("\n"))
  }

  /**
    * Print details of the response before it is returned.
    */
  val PrintResponse = ResponseFilters.Tap {
    response: Response =>
      println(Seq(s"***** RESPONSE ${response.status.code} *****",
        "Headers: " + response.headerMap,
        s"Content (${response.length}b):" + response.contentString
      ).mkString("\n"))
  }.andThen(ResponseFilters.TapFailure {
    t: Throwable =>
      println(s"***** RESPONSE FAILED *****")
      t.printStackTrace()
  })

  /**
    * Print details of a request and it's response.
    */
  val PrintRequestAndResponse = PrintRequest.andThen(PrintResponse)

}
