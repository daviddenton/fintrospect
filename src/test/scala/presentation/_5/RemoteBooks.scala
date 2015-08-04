package presentation._5

import com.twitter.finagle.Http
import com.twitter.util.Future
import io.fintrospect.RouteSpec
import io.fintrospect.parameters.Path
import io.fintrospect.util.HttpRequestResponseUtil.contentFrom
import org.jboss.netty.handler.codec.http.HttpMethod

object RemoteBooks {
  val titlePart = Path.string("titlePart")
  val route = RouteSpec().at(HttpMethod.GET) / "search" / titlePart
}

class RemoteBooks {

  private val client = RemoteBooks.route.bindToClient(Http.newService("localhost:10000"))

  def search(titlePart: String): Future[String] = client(RemoteBooks.titlePart --> titlePart).map(contentFrom(_))
}