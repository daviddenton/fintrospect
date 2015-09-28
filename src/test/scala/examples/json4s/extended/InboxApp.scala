package examples.json4s.extended

import com.twitter.finagle.Http
import com.twitter.finagle.http.filter.Cors
import com.twitter.finagle.http.path.Root
import examples.json4s.extended.Emails.InMemoryEmails
import io.fintrospect._
import io.fintrospect.renderers.swagger2dot0.Swagger2dot0Json

/**
 * This example uses Json4s as a JSON format instead of the Argo default
 */
object InboxApp extends App {

  val JsonLibrary = io.fintrospect.util.json.Json4s.Native // we define the JsonFormat here so we can import it in other places

  private val emails = new InMemoryEmails()

  private val inboxModule = FintrospectModule(Root / "inbox", Swagger2dot0Json(ApiInfo("Inbox Example", "1.0")))
    .withRoute(new EmailList(emails).route)
    .withRoute(new UserList(emails).route)

  Http.serve(":8181", new CorsFilter(Cors.UnsafePermissivePolicy).andThen(inboxModule.toService))

  println("See the service description at: http://localhost:8181/inbox")

  Thread.currentThread().join()
}

