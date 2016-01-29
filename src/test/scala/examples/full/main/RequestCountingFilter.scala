package examples.full.main

import java.io.PrintStream

import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.util.Future

/**
  * Filter used to track and report number of successful requests to the service
  */
class RequestCountingFilter(out: PrintStream) extends SimpleFilter[Request, Response] {
  private var successes = 0

  override def apply(request: Request, service: Service[Request, Response]): Future[Response] = {
    service(request)
      .onSuccess(r => {
        successes += 1
        if(successes % 10 == 0) {
          out.println(s"$successes successful requests served!")
        }
      })
  }
}
