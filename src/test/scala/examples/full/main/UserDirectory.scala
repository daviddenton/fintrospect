package examples.full.main

import com.twitter.finagle.Http
import com.twitter.finagle.http.Method.{Get, Post}
import com.twitter.finagle.http.Status._
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
    val email = Path(ParameterSpec[EmailAddress]("email", None, StringParamType, s => EmailAddress(s), _.value.toString))
    val route = RouteSpec().at(Get) / "user" / "email" / email
  }

}

/**
 * Remote User Directory service, accessible over HTTP
 */
class UserDirectory(hostAuthority: String) {
  private val http = Http.newService(hostAuthority)

  private val createClient = Create.route bindToClient http

  def create(name: Username, inEmail: EmailAddress): Future[User] = {
    val form = Form(Create.username --> name.value, Create.email --> inEmail.value)
    createClient(Create.form --> form)
      .map(Create.user.<--)
  }

  private val deleteClient = Delete.route bindToClient http

  def delete(user: User): Future[Unit] = deleteClient(Delete.id --> user.id).map(r => Unit)

  private val listClient = UserList.route bindToClient http

  def list(): Future[Seq[User]] = listClient().map(UserList.users.<--)

  private val lookupClient = Lookup.route bindToClient http

  def lookup(inEmail: EmailAddress): Future[Option[User]] =
    lookupClient(Lookup.email --> inEmail)
      .map { r => r.status match {
      case Ok => Some(Lookup.user <-- r)
      case NotFound => None
    }
    }
}
