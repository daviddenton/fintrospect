package examples.full.test.env

import com.twitter.finagle.Service
import com.twitter.finagle.http.Status._
import com.twitter.finagle.http.{Request, Response}
import examples.full.main.UserDirectory._
import examples.full.main._
import io.fintrospect.ServerRoutes
import io.fintrospect.formats.json.Json4s.Native.JsonFormat._
import io.fintrospect.formats.json.Json4s.Native.ResponseBuilder._

import scala.collection.mutable

/**
  * Fake implementation of the User Directory HTTP contract. Note the re-use of the RouteSpecs from UserDirectory.
  */
class FakeUserDirectory extends ServerRoutes[Response] {

  private var users: mutable.Map[Id, User] = null

  def contains(newUser: User): Unit = users(newUser.id) = newUser

  def reset() = users = mutable.Map[Id, User]()

  private def create() = Service.mk[Request, Response] {
    request => {
      val form = UserDirectory.Create.form <-- request
      val (username, email) = form <--(Create.username, Create.email)
      val newUser = User(Id(users.size), Username(username), EmailAddress(email))
      users(newUser.id) = newUser
      Created(encode(newUser))
    }
  }

  add(UserDirectory.Create.route.bindTo(create))

  private def delete(id: Id) = Service.mk[Request, Response] {
    request => users
      .get(id)
      .map { user => users -= id; Ok().toFuture }
      .getOrElse(NotFound())
  }

  add(UserDirectory.Delete.route.bindTo(delete))

  private def lookup(username: Username) = Service.mk[Request, Response] {
    request =>
      users
        .values
        .find(_.name == username)
        .map { found => Ok(encode(found)).toFuture }
        .getOrElse(NotFound())
  }

  add(UserDirectory.Lookup.route.bindTo(lookup))

  private def userList() = Service.mk[Request, Response] { _ =>  Ok(encode(users.values)) }

  add(UserDirectory.UserList.route.bindTo(userList))

  reset()
}
