package examples.oauth

import java.util.Date

import com.twitter.finagle.oauth2.{AccessToken, AuthInfo, DataHandler}
import com.twitter.util.Future

/**
  * Extremely contrived OAuth access control mechanism.
  */
class UserDataHandler extends DataHandler[User] {

  private val knownUser = User("admin")

  override def validateClient(clientId: String, clientSecret: String, grantType: String): Future[Boolean] = Future.value(
    clientSecret == "secret"
  )

  override def findClientUser(clientId: String, clientSecret: String, scope: Option[String]) = Future.value(
    if (clientSecret == "secret") Option(knownUser)
    else None
  )

  override def createAccessToken(authInfo: AuthInfo[User]): Future[AccessToken] = Future.value(
    accessToken()
  )

  override def refreshAccessToken(authInfo: AuthInfo[User], refreshToken: String) = Future.value(
    accessToken()
  )

  override def findAuthInfoByRefreshToken(refreshToken: String) = Future.value(
    if (refreshToken == "refresh") Option(authInfo)
    else None
  )

  override def getStoredAccessToken(authInfo: AuthInfo[User]) = Future.value(
    if (authInfo.user == knownUser) Option(accessToken())
    else None
  )

  override def findAuthInfoByAccessToken(accessToken: AccessToken) = Future.value(
    if (accessToken.token == "token") Option(authInfo)
    else None
  )

  def authInfo: AuthInfo[User] = {
    AuthInfo(knownUser, "clientId", Option("Scope"), None)
  }

  override def findAuthInfoByCode(code: String) = Future.value(
    if (code == "code") Option(authInfo)
    else None
  )

  override def findUser(username: String, password: String) = Future.value(
    if (username == password.reverse) Option(knownUser)
    else None
  )

  override def findAccessToken(token: String) = Future.value(
    if (token == "token") Option(accessToken())
    else None
  )

  def accessToken(): AccessToken = {
    AccessToken("token", Option("refresh"), Option("Scope"), Option(1000), new Date())
  }
}