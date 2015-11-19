package examples.full.test.contract

import com.twitter.finagle.http.Status._
import com.twitter.util.Await
import examples.full.main._
import examples.full.test.env.FakeEntryLoggerState
import io.fintrospect.testing.TestHttpServer
import org.scalatest.BeforeAndAfter

/**
  * Contract implementation for the Fake. We also test failure modes of our client here..
  */
class FakeEntryLoggerContractTest extends EntryLoggerContract with BeforeAndAfter {
  private lazy val port = 50001

  override lazy val authority = s"localhost:$port"

  private val server = new TestHttpServer(port, new FakeEntryLoggerState())

  before {
    Await.result(server.start())
  }

  after {
    Await.result(server.stop())
  }

  describe("the client responds as expected to failure conditions") {

    describe("log list") {
      it("returns a RemoteException if the response status is not Created") {
        server.respondWith(NotFound)
        intercept[RemoteSystemProblem](Await.result(entryLogger.list())) shouldBe RemoteSystemProblem("entry logger", NotFound)
      }
    }
    describe("log entry") {
      it("returns a RemoteException if the response status is not OK") {
        server.respondWith(NotFound)
        intercept[RemoteSystemProblem](Await.result(entryLogger.enter(User(Id(1), Username("bob"), EmailAddress("bob@bob.com"))))) shouldBe RemoteSystemProblem("entry logger", NotFound)
      }
    }
    describe("log exit") {
      it("returns a RemoteException if the response status is not OK") {
        server.respondWith(NotFound)
        intercept[RemoteSystemProblem](Await.result(entryLogger.exit(User(Id(1), Username("bob"), EmailAddress("bob@bob.com"))))) shouldBe RemoteSystemProblem("entry logger", NotFound)
      }
    }
  }
}
