package examples.full.test.feature

import com.twitter.finagle.http.Method.Post
import com.twitter.finagle.http.Request
import com.twitter.finagle.http.Status._
import examples.full.main._
import examples.full.test.env.{ResponseStatusAndContent, RunningTestEnvironment}
import org.scalatest.{FunSpec, ShouldMatchers}

class ExitingTest extends FunSpec with ShouldMatchers with RunningTestEnvironment {

  describe("exit endpoint") {
    it("rejects missing username in exit endpoint") {
      exitBuilding(None, "realSecret").status shouldBe BadRequest
    }

    it("is protected with a secret key") {
      exitBuilding(Option("Bob"), "fakeSecret").status shouldBe Unauthorized
    }
  }

  describe("when a user is not in the building") {
    it("does not allow user to exit") {
      exitBuilding(Option("Bob"), "realSecret").status shouldBe BadRequest
    }
  }

  describe("when a known user tries to exit") {
    it("allows the user to exit and logs exit") {
      env.userDirectory.contains(User(Id(1), Username("Bob"), EmailAddress("bob@bob.com")))
      enterBuilding(Option("Bob"), "realSecret")
      exitBuilding(Option("Bob"), "realSecret").status shouldBe Accepted

      env.entryLogger.entries shouldBe Seq(
        UserEntry("Bob", goingIn = true, env.clock.millis()),
        UserEntry("Bob", goingIn = false, env.clock.millis())
      )
    }
  }

  private def enterBuilding(user: Option[String], secret: String): ResponseStatusAndContent = {
    val query = user.map("username=" + _).getOrElse("")
    val request = Request(Post, "/security/knock?" + query)
    request.headerMap("key") = secret
    env.responseTo(request)
  }

  private def exitBuilding(user: Option[String], secret: String): ResponseStatusAndContent = {
    val query = user.map("username=" + _).getOrElse("")
    val request = Request(Post, "/security/bye?" + query)
    request.headerMap("key") = secret
    env.responseTo(request)
  }
}
