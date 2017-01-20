package cookbook.core

import com.twitter.io.Bufs
import io.fintrospect.ContentType
import io.fintrospect.formats.{AbstractResponseBuilder, ResponseBuilder}

// fintrospect-core
object Custom_Response_Format_Example extends App {

  val identify: Service[Request, Response] = Service.mk { req: Request => Ok(List("this", "is", "comma", "separated", "hello", "world")) }

  import com.twitter.finagle.http.Method.Get
  import com.twitter.finagle.http.path.Root
  import com.twitter.finagle.http.{Request, Response}
  import com.twitter.finagle.{Http, Service}
  import com.twitter.util.Await.ready
  import cookbook.core.Custom_Response_Format_Example.Csv.ResponseBuilder._
  import io.fintrospect.{Module, RouteModule, RouteSpec, ServerRoute}
  val route: ServerRoute[Request, Response] = RouteSpec().at(Get) bindTo identify
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
