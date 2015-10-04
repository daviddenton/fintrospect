package presentation._6

import com.twitter.finagle.Httpx
import com.twitter.finagle.httpx.Method._
import com.twitter.util.Future
import io.fintrospect.RouteSpec
import io.fintrospect.parameters.Path
import io.fintrospect.util.HttpRequestResponseUtil.contentFrom

object RemoteBooks {
  val titlePart = Path.string("titlePart")
  val route = RouteSpec().at(Get) / "search" / titlePart
}

class RemoteBooks {

  private val client = RemoteBooks.route.bindToClient(Httpx.newService("localhost:10000"))

  def search(titlePart: String): Future[String] = client(RemoteBooks.titlePart --> titlePart).map(contentFrom(_))
}
