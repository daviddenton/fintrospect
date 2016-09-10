import com.twitter.finagle.http.Method.Get
import com.twitter.finagle.http.filter.Cors
import com.twitter.finagle.http.filter.Cors.HttpFilter
import com.twitter.finagle.http.path.Root
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Http, Service}
import io.fintrospect.formats.{MsgPack, MsgPackMsg}
import io.fintrospect.{ModuleSpec, RouteSpec}

object MsgPackApp extends App {

  case class Bob(asd: String)

  private val inboxModule = ModuleSpec(Root)
    .withRoute(RouteSpec().at(Get) bindTo Service.mk[Request, Response] {
      r => MsgPack.ResponseBuilder.OK(MsgPackMsg(Bob("asddsa")))
    })

  Http.serve(":8181", new HttpFilter(Cors.UnsafePermissivePolicy).andThen(inboxModule.toService))

  println("See the service description at: http://localhost:8181/inbox")

  Thread.currentThread().join()


}
