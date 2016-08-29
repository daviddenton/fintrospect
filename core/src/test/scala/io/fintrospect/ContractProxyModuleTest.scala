package io.fintrospect

import com.twitter.finagle.Service
import com.twitter.finagle.http.Method.Get
import com.twitter.finagle.http.{Request, Status}
import com.twitter.util.Await
import io.fintrospect.formats.PlainText.ResponseBuilder.implicits._
import io.fintrospect.parameters.Query
import org.scalatest.{FunSpec, Matchers}

object TestContract extends Contract {

  object Endpoint extends ContractEndpoint {
    val query = Query.required.string("query")
    override val route = RouteSpec().taking(query).at(Get) / "hello"
  }
}

class ContractProxyModuleTest extends FunSpec with Matchers {

  describe("ContractProxyModule") {
    it("cretes service which proxies requests to the underlying service") {
      val svc = Service.mk { req: Request => Status.Ok(TestContract.Endpoint.query <-- req) }
      Await.result(ContractProxyModule("remote", svc, TestContract).toService(Request("/hello?query=value"))).contentString shouldBe "value"
    }
  }
}
