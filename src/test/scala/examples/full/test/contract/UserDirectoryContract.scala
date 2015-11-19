package examples.full.test.contract

import com.twitter.util.Await
import examples.full.main._
import org.scalatest.{FunSpec, ShouldMatchers}

/**
 * This represents the contract that both the real and fake UserDirectory servers will adhere to.
 */
trait UserDirectoryContract extends FunSpec with ShouldMatchers {
  def authority: String

  val userDirectory = new UserDirectory(authority)

  val username: Username
  val email: EmailAddress

  var user: User = null

  it("is empty initially") {
    Await.result(userDirectory.lookup(email)) shouldBe None
    Await.result(userDirectory.list()) shouldBe Nil
  }

  it("can create a user") {
    user = Await.result(userDirectory.create(username, email))

    user.name shouldBe username
    user.email shouldBe email
  }

  it("can lookup a user by email") {
    val foundUser = Await.result(userDirectory.lookup(email))
    foundUser shouldBe Some(user)
  }

  it("can list users") {
    val users = Await.result(userDirectory.list())

    users.length shouldBe 1
    users.head.name shouldBe username
    users.head.email shouldBe email
  }

  it("can delete user") {
    Await.result(userDirectory.delete(user))
    Await.result(userDirectory.lookup(email)) shouldBe None
    Await.result(userDirectory.list()) shouldBe Nil
  }
}


