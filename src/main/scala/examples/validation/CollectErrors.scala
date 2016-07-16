package examples.validation


import java.time.LocalDate

import com.twitter.finagle.http.Request
import io.fintrospect.parameters.Query
import io.fintrospect.util.Validator

object CollectErrors extends App {

  val millennium = LocalDate.of(2000, 1, 1)

  /**
    * Because we are interested in collecting ALL of the errors, we can't use cross-field extraction here
    * - use a Validation instead
    */
  def validate(input: Request) = Validator.mk(
    Query.required.localDate("theFuture") <--?(input, "Must be after the millennium", _.isAfter(millennium)),
    Query.optional.localDate("anyOldDate") <--? input,
    Query.optional.localDate("thePast") <--?(input, "Must be before the millennium", _.isBefore(millennium))
  ) {
    case (future, old, past) => s"validated ok: $future, $old, $past"
  }

  println(validate(Request("?theFuture=2010-01-01&anyOldDate=2000-01-01&thePast=1999-01-01")))
  println(validate(Request("?theFuture=2010-01-01&anyOldDate=NOTADATE-01-01&thePast=2003-01-01")))
}
