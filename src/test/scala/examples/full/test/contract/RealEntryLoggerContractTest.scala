package examples.full.test.contract

import org.scalatest.{BeforeAndAfter, Ignore}

/**
 * Contract implementation for the real Entry Logger service. Extra steps might be required here to setup/teardown
 * test data.
 */
@Ignore // this would not be ignored in reality
class RealEntryLoggerContractTest extends EntryLoggerContract with BeforeAndAfter {

  // real test data would be set up here for the required environment
  override lazy val authority = "google.com:80"
}
