package cookbook.core

import com.twitter.finagle.Service
import com.twitter.io.Bufs
import io.fintrospect.ContentType
import io.fintrospect.formats.{AbstractResponseBuilder, ResponseBuilder}

// fintrospect-core
object Custom_Response_Format_Example extends App {

  val service: Service[Request, Response] = Service.mk { req: Request => Ok(List("this", "is", "comma", "separated", "hello", "world")) }

  import Csv.ResponseBuilder._
  import com.twitter.finagle.Http
  import com.twitter.finagle.http.Method.Get
  import com.twitter.finagle.http.path.Root
  import com.twitter.finagle.http.{Request, Response}
  import com.twitter.util.Await.ready
  import io.fintrospect.{Module, RouteModule, RouteSpec, ServerRoute}
  val route: ServerRoute[Request, Response] = RouteSpec().at(Get) bindTo service
  val module: Module = RouteModule(Root).withRoute(route)

  object Csv {
    object ResponseBuilder extends AbstractResponseBuilder[List[String]] {
      override def HttpResponse(): ResponseBuilder[List[String]] = new ResponseBuilder(
        (i: List[String]) => Bufs.utf8Buf(i.mkString(",")),
        error => List("ERROR:" + error),
        throwable => List("ERROR:" + throwable.getMessage),
        ContentType("application/csv")
      )
    }
  }


  ready(Http.serve(":9999", module.toService))
}

//curl -v http://localhost:9999
