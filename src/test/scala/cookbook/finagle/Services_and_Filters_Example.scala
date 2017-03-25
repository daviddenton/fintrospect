package cookbook.finagle

import com.twitter.finagle.{Filter, Service}
import com.twitter.util.{Await, Future}

object Services_and_Filters_Example extends App {

  val doubler = Service.mk[Int, Int] { in => Future(in + in) }

  val squareRoot = Filter.mk[Int, Double, Int, Int] { (req, next) => next(req).map(Math.sqrt(_)) }

  val convert = Filter.mk[String, String, Int, Double] { (req, next) => next(req.toInt).map(_.toString) }

  // read right to left:
  val stringDoubler = convert.andThen(squareRoot).andThen(doubler)

  println(Await.result(stringDoubler("50")))
}
