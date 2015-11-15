package examples.full.test.contract

import com.twitter.util.Await
import examples.full.main.{EmailAddress, Username}
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

  private val userDirectory = new TestHttpServer(port, new FakeUserDirectoryState())

  before {
    Await.result(userDirectory.start())
  }

  after {
    Await.result(userDirectory.stop())
  }
}
