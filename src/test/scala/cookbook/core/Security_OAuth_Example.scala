package cookbook.core

import java.util.Date

import com.twitter.finagle.oauth2.{AccessToken, AuthInfo, DataHandler}
import com.twitter.util.Future

case class User(name: String)

class UserDataHandler extends DataHandler[User] {

  private val knownUser = User("admin")

  override def validateClient(clientId: String, clientSecret: String, grantType: String) =
    Future(clientSecret == "secret")

  override def findClientUser(clientId: String, clientSecret: String, scope: Option[String]) =
    Future(if (clientSecret == "secret") Option(knownUser) else None)

  override def createAccessToken(authInfo: AuthInfo[User]) = Future(accessToken())

  override def refreshAccessToken(authInfo: AuthInfo[User], refreshToken: String) = Future(accessToken())

  override def findAuthInfoByRefreshToken(refreshToken: String) =
    Future(if (refreshToken == "refresh") Option(authInfo()) else None)

  override def getStoredAccessToken(authInfo: AuthInfo[User]) =
    Future(if (authInfo.user == knownUser) Option(accessToken()) else None)

  override def findAuthInfoByAccessToken(accessToken: AccessToken) =
    Future(if (accessToken.token == "token") Option(authInfo()) else None)

  override def findAuthInfoByCode(code: String) =
    Future(if (code == "code") Option(authInfo()) else None)

  override def findUser(username: String, password: String) =
    Future(if (username == password.reverse) Option(knownUser) else None)

  override def findAccessToken(token: String) =
    Future(if (token == "token") Option(accessToken()) else None)

  private def authInfo() = AuthInfo(knownUser, "clientId", Option("Scope"), None)

  private def accessToken() = AccessToken("token", Option("refresh"), Option("Scope"), Option(1000), new Date())
}

object Security_OAuth_Example extends App {

  import com.twitter.finagle.http.Method.Get
  import com.twitter.finagle.http.path.Root
  import com.twitter.finagle.http.{Request, Response}
  import com.twitter.finagle.oauth2.OAuthErrorInJson
  import com.twitter.finagle.{Filter, Http, OAuth2Filter, OAuth2Request, Service}
  import com.twitter.util.Await.ready
  import io.fintrospect.formats.PlainText.ResponseBuilder.Ok
  import io.fintrospect.{Module, RouteModule, RouteSpec, ServerRoute}

  val protectedSvc: Service[OAuth2Request[User], Response] = Service.mk {
    rq: OAuth2Request[User] => Ok(rq.authInfo.user.name)
  }

  val authFilter: Filter[Request, Response, OAuth2Request[User], Response] =
    new OAuth2Filter(new UserDataHandler) with OAuthErrorInJson

  val route: ServerRoute[Request, Response] = RouteSpec().at(Get) bindTo authFilter.andThen(protectedSvc)

  val module: Module = RouteModule(Root).withRoute(route)

  ready(Http.serve(":9999", module.toService))
}

//curl -v http://localhost:9999/
//curl -v -H"access_token: secret" http://localhost:9999/