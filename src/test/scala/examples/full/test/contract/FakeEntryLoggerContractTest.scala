package examples.full.test.contract

import com.twitter.finagle.http.Status._
import com.twitter.util.Await
import examples.full.main._
import examples.full.test.env.RunningTestEnvironment
import org.scalatest.BeforeAndAfter

/**
  * Contract implementation for the Fake. We also test failure modes of our client here..
  */
class FakeEntryLoggerContractTest extends EntryLoggerContract with BeforeAndAfter with RunningTestEnvironment {
  override lazy val authority = s"localhost:$entryLoggerPort"

  describe("the client responds as expected to failure conditions") {

    describe("log list") {
      it("returns a RemoteException if the response status is not Created") {
        env.entryLoggerServer.respondWith(NotFound)
        intercept[RemoteSystemProblem](Await.result(entryLogger.list())) shouldBe RemoteSystemProblem("entry logger", NotFound)
      }
    }
    describe("log entry") {
      it("returns a RemoteException if the response status is not OK") {
        env.entryLoggerServer.respondWith(NotFound)
        intercept[RemoteSystemProblem](Await.result(entryLogger.enter(Username("bob")))) shouldBe RemoteSystemProblem("entry logger", NotFound)
      }
    }
    describe("log exit") {
      it("returns a RemoteException if the response status is not OK") {
        env.entryLoggerServer.respondWith(NotFound)
        intercept[RemoteSystemProblem](Await.result(entryLogger.exit(Username("bob")))) shouldBe RemoteSystemProblem("entry logger", NotFound)
      }
    }
  }
}
