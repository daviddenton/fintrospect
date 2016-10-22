package examples.circe

import com.twitter.finagle.Http
import com.twitter.finagle.http.filter.Cors
import com.twitter.finagle.http.filter.Cors.HttpFilter
import com.twitter.finagle.http.path.Root
import com.twitter.util.Await
import examples.circe.Emails.InMemoryEmails
import io.fintrospect.ModuleSpec
import io.fintrospect.renderers.swagger2dot0.{ApiInfo, Swagger2dot0Json}

/**
  * This example uses Circe as a JSON format instead of the Argo default. The 4 endpoints show the different methods of
  * returning values
  */
object InboxApp extends App {

  private val emails = new InMemoryEmails()

  private val inboxModule = ModuleSpec(Root / "inbox", Swagger2dot0Json(ApiInfo("Inbox Example", "1.0")))
    .withRoute(new AddMessage(emails).route)
    .withRoute(new EmailList(emails).route)
    .withRoute(new FindUserWithEmail(emails).route)
    .withRoute(new UserList(emails).route)

  println("See the service description at: http://localhost:8181/inbox")

  Await.ready(
    Http.serve(":8181", new HttpFilter(Cors.UnsafePermissivePolicy).andThen(inboxModule.toService))
  )
}

