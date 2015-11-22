package examples.full.test.contract

import examples.full.main.{EmailAddress, Username}
import org.scalatest.Ignore

/**
 * Contract implementation for the real user directory service. Extra steps might be required here to setup/teardown
 * test data.
 */
@Ignore // this would not be ignored in reality
class RealUserDirectoryContractTest extends UserDirectoryContract {

  // real test data would be set up here for the required environment
  override lazy val username = Username("Elon Musk")
  override lazy val email = EmailAddress("elon@tesla.com")
  override lazy val authority = "google.com:80"
}
