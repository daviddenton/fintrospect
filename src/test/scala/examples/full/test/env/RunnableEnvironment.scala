package examples.full.test.env

import io.fintrospect.testing.TestHttpServer

object RunnableEnvironment extends App {

  private val userDirectoryPort = 10000
  private val entryLoggerPort = 10001

  new TestHttpServer(userDirectoryPort, new FakeUserDirectoryState()).start()
  new TestHttpServer(entryLoggerPort, new FakeEntryLoggerState()).start()

  Thread.currentThread().join()
}
