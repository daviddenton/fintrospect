package examples.full.test.feature

import com.twitter.finagle.http.Method.Post
import com.twitter.finagle.http.Request
import com.twitter.finagle.http.Status._
import examples.full.main._
import examples.full.test.env.RunningTestEnvironment
import org.scalatest.{FunSpec, ShouldMatchers}

class AttemptToGainEntryTest extends FunSpec with ShouldMatchers with RunningTestEnvironment {

  describe("attempt to enter building") {
    describe("for an unknown user") {
      it("does not allow user in") {
        val request = Request(Post, "/security/knock?username=Rita")
        request.headerMap("key") = "realSecret"
        val (status, _) = env.responseTo(request)
        status shouldBe NotFound
      }
    }

    describe("for a known user") {
      it("rejects when key is incorrect") {
        val request = Request(Post, "/security/knock?username=Bob")
        request.headerMap("key") = "fakeSecret"
        val (status, _) = env.responseTo(request)
        status shouldBe Unauthorized
      }

      describe("does allow user in") {
        env.userDirectory.contains(User(Id(1), Username("Bob"), EmailAddress("bob@bob.com")))

        it("allows the user in") {
          val request = Request(Post, "/security/knock?username=Bob")
          request.headerMap("key") = "realSecret"
          val (status, _) = env.responseTo(request)
          status shouldBe Ok
        }

        it("logs entry") {
          env.entryLogger.entries shouldBe Seq(UserEntry("Bob", goingIn = true, env.clock.millis()))
        }

        it("allows the user to exit") {
          val request = Request(Post, "/security/bye?username=Bob")
          request.headerMap("key") = "realSecret"
          val (status, _) = env.responseTo(request)
          status shouldBe Ok
        }

        it("logs exit") {
          env.entryLogger.entries shouldBe Seq(
            UserEntry("Bob", goingIn = true, env.clock.millis()),
            UserEntry("Bob", goingIn = false, env.clock.millis())
          )
        }
      }

    }
  }
}
