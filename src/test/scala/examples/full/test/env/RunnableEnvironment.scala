package examples.full.test.env

import io.fintrospect.testing.TestHttpServer

object RunnableEnvironment extends App {

  private val userDirectoryPort = 10000

  new TestHttpServer(userDirectoryPort, new FakeUserDirectoryState()).start()

}
