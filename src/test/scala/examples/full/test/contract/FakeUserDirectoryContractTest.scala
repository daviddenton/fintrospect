package examples.full.test.contract

import com.twitter.finagle.http.Status._
import com.twitter.util.Await
import examples.full.main._
import examples.full.test.env.RunningTestEnvironment

/**
  * Contract implementation for the Fake.
  */
class FakeUserDirectoryContractTest extends UserDirectoryContract with RunningTestEnvironment {
  override lazy val username = Username("Bob the Builder")
  override lazy val email = EmailAddress("bob@fintrospect.io")
  override lazy val authority = s"localhost:$userDirectoryPort"

  describe("the client responds as expected to failure conditions") {

    describe("list users") {
      it("returns a RemoteException if the response status is not OK") {
        env.userDirectoryServer.respondWith(InternalServerError)
        intercept[RemoteSystemProblem](Await.result(userDirectory.list())) shouldBe RemoteSystemProblem("user directory", InternalServerError)
      }
    }
    describe("create user") {
      it("returns a RemoteException if the response status is not Created") {
        env.userDirectoryServer.respondWith(InternalServerError)
        intercept[RemoteSystemProblem](Await.result(userDirectory.create(username, email))) shouldBe RemoteSystemProblem("user directory", InternalServerError)
      }
    }
    describe("delete user") {
      it("returns a RemoteException if the response status is not OK") {
        env.userDirectoryServer.respondWith(InternalServerError)
        intercept[RemoteSystemProblem](Await.result(userDirectory.delete(user))) shouldBe RemoteSystemProblem("user directory", InternalServerError)
      }
    }
    describe("lookup user") {
      it("returns a RemoteException if the response status is not OK") {
        env.userDirectoryServer.respondWith(InternalServerError)
        intercept[RemoteSystemProblem](Await.result(userDirectory.lookup(username))) shouldBe RemoteSystemProblem("user directory", InternalServerError)
      }
    }
  }

}
