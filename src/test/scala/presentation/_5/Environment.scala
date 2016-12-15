package presentation._5

import presentation.Books

object Environment extends App {
  new FakeRemoteLibrary(new Books)
  new SearchApp
  Thread.currentThread().join()
}

/**
  * showcase: swagger
  */