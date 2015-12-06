package examples.json4s

import com.twitter.finagle.Http
import com.twitter.finagle.http.filter.Cors
import com.twitter.finagle.http.filter.Cors.HttpFilter
import com.twitter.finagle.http.path.Root
import examples.json4s.Emails.InMemoryEmails
import io.fintrospect._
import io.fintrospect.formats.json.Json4s
import io.fintrospect.renderers.swagger2dot0.{ApiInfo, Swagger2dot0Json}

/**
 * This example uses Json4s as a JSON format instead of the Argo default
 */
object InboxApp extends App {

  val JsonLibrary = Json4s.Native // we define the JsonFormat once here so we can import it in other classes

  private val emails = new InMemoryEmails()

  private val inboxModule = ModuleSpec(Root / "inbox", Swagger2dot0Json(ApiInfo("Inbox Example", "1.0")))
    .withRoute(new AddMessage(emails).route)
    .withRoute(new EmailList(emails).route)
    .withRoute(new UserList(emails).route)

  Http.serve(":8181", new HttpFilter(Cors.UnsafePermissivePolicy).andThen(inboxModule.toService))

  println("See the service description at: http://localhost:8181/inbox")

  Thread.currentThread().join()
}

