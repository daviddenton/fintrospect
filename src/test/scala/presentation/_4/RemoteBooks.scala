package presentation._4

import com.twitter.finagle.Http
import com.twitter.finagle.http.Method.Get
import com.twitter.util.Future
import io.fintrospect.RouteSpec
import io.fintrospect.parameters.Path


object RemoteBooks {
  val titlePart = Path.string("titlePart")
  val route = RouteSpec().at(Get) / "search" / titlePart
}

class RemoteBooks {

  private val client = RemoteBooks.route.bindToClient(Http.newService("localhost:10000"))

  def search(titlePart: String): Future[String] = client(RemoteBooks.titlePart --> titlePart).map(_.contentString)
}
