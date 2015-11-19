package examples.full.test.contract

import com.twitter.finagle.http.Status._
import com.twitter.util.Await
import examples.full.main._
import examples.full.test.env.FakeUserDirectoryState
import io.fintrospect.testing.TestHttpServer
import org.scalatest.BeforeAndAfter

/**
 * Contract implementation for the Fake.
 */
class FakeUserDirectoryContractTest extends UserDirectoryContract with BeforeAndAfter {
  private lazy val port = 50000

  override lazy val username = Username("Bob the Builder")
  override lazy val email = EmailAddress("bob@fintrospect.io")
  override lazy val authority = s"localhost:$port"

  private val server = new TestHttpServer(port, new FakeUserDirectoryState())

  before {
    Await.result(server.start())
  }

  after {
    Await.result(server.stop())
  }

  describe("the client responds as expected to failure conditions") {

    describe("list users") {
      it("returns a RemoteException if the response status is not OK") {
        server.respondWith(InternalServerError)
        intercept[RemoteSystemProblem](Await.result(userDirectory.list())) shouldBe RemoteSystemProblem("user directory", InternalServerError)
      }
    }
    describe("create user") {
      it("returns a RemoteException if the response status is not Created") {
        server.respondWith(InternalServerError)
        intercept[RemoteSystemProblem](Await.result(userDirectory.create(username, email))) shouldBe RemoteSystemProblem("user directory", InternalServerError)
      }
    }
    describe("delete user") {
      it("returns a RemoteException if the response status is not OK") {
        server.respondWith(InternalServerError)
        intercept[RemoteSystemProblem](Await.result(userDirectory.delete(user))) shouldBe RemoteSystemProblem("user directory", InternalServerError)
      }
    }
    describe("lookup user") {
      it("returns a RemoteException if the response status is not OK") {
        server.respondWith(InternalServerError)
        intercept[RemoteSystemProblem](Await.result(userDirectory.lookup(email))) shouldBe RemoteSystemProblem("user directory", InternalServerError)
      }
    }
  }

}
