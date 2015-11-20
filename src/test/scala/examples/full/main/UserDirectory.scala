package examples.full.main

import com.twitter.finagle.Http
import com.twitter.finagle.http.Method.{Get, Post}
import com.twitter.finagle.http.Status._
import com.twitter.finagle.http.{Response, Status}
import com.twitter.util.Future
import examples.full.main.UserDirectory._
import io.fintrospect.RouteSpec
import io.fintrospect.formats.json.Json4s.Native.JsonFormat._
import io.fintrospect.parameters._

/**
  * Remote User Directory service, accessible over HTTP. We define the Routes making up the HTTP contract here so they can be
  * re-used to provide the Fake implementation which we can dev against.
  */
object UserDirectory {

  object Create {
    val user = Body(bodySpec[User](None))
    val email = FormField.required.string("email")
    val username = FormField.required.string("username")
    val form = Body.form(email, username)
    val route = RouteSpec().body(form).at(Post) / "user"
  }

  object Delete {
    val id = Path(ParameterSpec[Id]("id", None, NumberParamType, s => Id(Integer.parseInt(s)), _.value.toString))
    val username = FormField.required.string("username")
    val route = RouteSpec().at(Post) / "user" / "id" / id
  }

  object UserList {
    val users = Body(bodySpec[Seq[User]](None))
    val route = RouteSpec().at(Get) / "user"
  }

  object Lookup {
    val user = Body(bodySpec[User](None))
    val username = Path(ParameterSpec[Username]("username", None, StringParamType, s => Username(s), _.value.toString))
    val route = RouteSpec().at(Get) / "user" / username
  }

}

/**
  * Remote User Directory service, accessible over HTTP
  */
class UserDirectory(hostAuthority: String) {

  private def expect[T](expectedStatus: Status, b: Body[T]): Response => T = {
    r => if (r.status == expectedStatus) b <-- r else throw RemoteSystemProblem("user directory", r.status)
  }

  private val http = Http.newService(hostAuthority)

  private val createClient = Create.route bindToClient http

  def create(name: Username, inEmail: EmailAddress): Future[User] = {
    val form = Form(Create.username --> name.value, Create.email --> inEmail.value)
    createClient(Create.form --> form)
      .map(expect(Created, Create.user))
  }

  private val deleteClient = Delete.route bindToClient http

  def delete(user: User): Future[Unit] =
    deleteClient(Delete.id --> user.id)
      .map(r => if (r.status == Ok) Unit else throw RemoteSystemProblem("user directory", r.status))

  private val listClient = UserList.route bindToClient http

  def list(): Future[Seq[User]] = listClient()
    .map(expect(Ok, UserList.users))

  private val lookupClient = Lookup.route bindToClient http

  def lookup(username: Username): Future[Option[User]] =
    lookupClient(Lookup.username --> username)
      .map { r => r.status match {
        case Ok => Some(Lookup.user <-- r)
        case NotFound => None
        case s => throw RemoteSystemProblem("user directory", r.status)
      }
      }
}
