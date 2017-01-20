package cookbook.core


// fintrospect-core
object Custom_Response_Format_Example extends App {
  import com.twitter.finagle.http.Method.Get
  import com.twitter.finagle.http.path.Root
  import com.twitter.finagle.http.{Request, Response}
  import com.twitter.finagle.{Http, Service}
  import com.twitter.io.Bufs
  import com.twitter.util.Await.ready
  import io.fintrospect.formats.{AbstractResponseBuilder, ResponseBuilder}
  import io.fintrospect.{ContentType, Module, RouteModule, RouteSpec, ServerRoute}

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

  import Csv.ResponseBuilder._


  val service: Service[Request, Response] = Service.mk { _: Request => Ok(List("this", "is", "comma", "separated", "hello", "world")) }

  val route: ServerRoute[Request, Response] = RouteSpec().at(Get) bindTo service
  val module: Module = RouteModule(Root).withRoute(route)

  ready(Http.serve(":9999", module.toService))
}

//curl -v http://localhost:9999
