package examples.full.test.env

import com.twitter.finagle.Service
import com.twitter.finagle.http.Status._
import com.twitter.finagle.http.{Request, Response}
import examples.full.main.UserDirectory._
import examples.full.main.{EmailAddress, Id, User, Username}
import io.fintrospect.ServerRoutes
import io.fintrospect.formats.json.Json4s.Native.JsonFormat._
import io.fintrospect.formats.json.Json4s.Native.ResponseBuilder._

import scala.collection.mutable

class FakeUserDirectoryState extends ServerRoutes {

  private val users = mutable.Map[Id, User]()

  private def create() = new Service[Request, Response] {
    override def apply(request: Request) = {
      val form = Create.form <-- request
      val (username, email) = form <--(Create.username, Create.email)
      val newUser = User(Id(users.size), Username(username), EmailAddress(email))
      users(newUser.id) = newUser
      Created(encode(newUser))
    }
  }

  add(Create.route.bindTo(create))

  private def delete(id: Id) = new Service[Request, Response] {
    override def apply(request: Request) = users
      .get(id)
      .map { user => users -= id; Ok().toFuture }
      .getOrElse(NotFound())
  }

  add(Delete.route.bindTo(delete))

  private def lookup(email: EmailAddress) = new Service[Request, Response] {
    override def apply(request: Request) = users
      .values
      .find(_.email == email)
      .map { found => Ok(encode(found)).toFuture }
      .getOrElse(NotFound())
  }

  add(Lookup.route.bindTo(lookup))

  private def userList() = new Service[Request, Response] {
    override def apply(request: Request) = Ok(encode(users.values))
  }

  add(UserList.route.bindTo(userList))
}
