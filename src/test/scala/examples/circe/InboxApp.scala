package examples.circe

import com.twitter.finagle.Http
import com.twitter.finagle.http.filter.Cors
import com.twitter.finagle.http.filter.Cors.HttpFilter
import com.twitter.finagle.http.path.Root
import examples.circe.Emails.InMemoryEmails
import io.fintrospect.ModuleSpec
import io.fintrospect.renderers.swagger2dot0.{ApiInfo, Swagger2dot0Json}

/**
 * This example uses Circe as a JSON format instead of the Argo default
 */
object InboxApp extends App {

  private val emails = new InMemoryEmails()

  private val inboxModule = ModuleSpec(Root / "inbox", Swagger2dot0Json(ApiInfo("Inbox Example", "1.0")))
    .withRoute(new AddMessage(emails).route)
    .withRoute(new EmailList(emails).route)
    .withRoute(new UserList(emails).route)

  Http.serve(":8181", new HttpFilter(Cors.UnsafePermissivePolicy).andThen(inboxModule.toService))

  println("See the service description at: http://localhost:8181/inbox")

  Thread.currentThread().join()
}

