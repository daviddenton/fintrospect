package examples.formvalidation

import com.twitter.util.Future

/**
  * Represents a remote system - ie. Async
  */
class GreetingDatabase {
  def lookupGreeting(age: Age, name: Name): Future[Option[String]] = {
    if (age.value > 60) Future(Option("Greetings"))
    else if (age.value < 25) Future(Option("Yo!"))
    else Future(None)
  }
}
