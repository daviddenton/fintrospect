package examples.full.test.env

object RunnableEnvironment extends App {
  val serverPort = 9999
  val userDirectoryPort = 10000
  val entryLoggerPort = 10001

  new TestEnvironment(serverPort, userDirectoryPort, entryLoggerPort).start()
  Thread.currentThread().join()
}
