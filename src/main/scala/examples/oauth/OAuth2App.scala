package examples.oauth

import com.twitter.finagle.http.Method.Get
import com.twitter.finagle.http.Status.Ok
import com.twitter.finagle.http.filter.Cors
import com.twitter.finagle.http.filter.Cors.HttpFilter
import com.twitter.finagle.http.path.Root
import com.twitter.finagle.oauth2.OAuthErrorInJson
import com.twitter.finagle.{Http, OAuth2Filter, OAuth2Request, Service}
import com.twitter.util.Await
import io.fintrospect.formats.PlainText.ResponseBuilder.implicits.statusToResponseBuilderConfig
import io.fintrospect.renderers.simplejson.SimpleJson
import io.fintrospect.{ModuleSpec, RouteSpec}

/**
  * OAuth controlled app using finagle-oath2. Use the "access_token" of "token" to gain access to the services.
  * This example shows how you can vary the input of the request to the service endpoints by altering the type
  * using the module filter - in this case Request --> OAuth2Request[User]
  */
object OAuth2App extends App {
  val auth = new OAuth2Filter(new UserDataHandler) with OAuthErrorInJson

  val module = ModuleSpec(Root / "auth", SimpleJson(), auth)
    .withRoute(RouteSpec().at(Get) / "user" bindTo Service.mk {
      rq: OAuth2Request[User] => Ok(rq.authInfo.user.name)
    })

  private val service = module.toService

  println("See the service description (passing access_token of \"token\") at: http://localhost:8181/auth")

  Await.ready(
    Http.serve(":8181", new HttpFilter(Cors.UnsafePermissivePolicy).andThen(service))
  )
}
