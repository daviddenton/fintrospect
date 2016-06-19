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
    req: Request => {
      println(s"***** REQUEST: ${req.uri} *****")
      println("Headers: " + req.headerMap)
      println("Params: " + req.params)
      println(s"Content (${req.contentString.length}b):" + req.contentString)
    }
  }

  /**
    * Print details of the response before it is returned.
    */
  val PrintResponse = ResponseFilters.Tap {
    response: Response => {
      println(s"***** RESPONSE ${response.status.code} *****")
      println("Headers: " + response.headerMap)
      println(s"Content (${response.contentString.length}b):" + response.contentString)
    }
  }.andThen(ResponseFilters.TapFailure {
    t: Throwable => {
      println(s"***** RESPONSE FAILED *****")
      t.printStackTrace()
    }
  })

  /**
    * Print details of a request and it's response.
    */
  val PrintRequestAndResponse = PrintRequest.andThen(PrintResponse)

}
