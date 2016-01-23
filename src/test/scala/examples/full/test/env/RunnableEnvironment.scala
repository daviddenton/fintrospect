package examples.full.test.env

import examples.full.main.{EmailAddress, Id, User, Username}

object RunnableEnvironment extends App {
  val serverPort = 9999
  val userDirectoryPort = 10000
  val entryLoggerPort = 10001

  private val environment: TestEnvironment = new TestEnvironment(serverPort, userDirectoryPort, entryLoggerPort)
  environment.start()
  environment.userDirectory.contains(User(Id(1), Username("Bob"), EmailAddress("bob@bob.com")))
  environment.userDirectory.contains(User(Id(2), Username("Rita"), EmailAddress("rita@bob.com")))
  environment.userDirectory.contains(User(Id(3), Username("Sue"), EmailAddress("sue@bob.com")))
  Thread.currentThread().join()
}
