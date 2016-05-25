# cross-field validation
Further to the process of retrieving them from the request, there exists the problem of validating that the passed parameters 
are actually logically valid when passed together. For example, a date range is only valid when the start date is before the end date. 

For this purpose, you can use an `Extractable` - a trait which provides a single method `<--?()` to return one of 3 
states: `Extracted(value)`, `NotProvided` (for optional values which are not supplied), and `ExtractionFailed(parameter)` for missing or 
invalid values. These constructs can be used inside a for comprehension to provide cross-field validation, and eventual creation of a target 
object. Below is a service that implements this logic - note the use of a predicate and a failure reason which provides the logic for the check:
```
case class DateRange(startDate: LocalDate, endDate: Option[LocalDate])

val range: Extractable[Request, DateRange] = Extractable.mk {
  (request: Request) =>
      for {
        startDate <- Query.required.localDate("start") <--? request
        endDate <- Query.required.localDate("end") <--?(request, "end date invalid", _.isAfter(startDate.get))
      } yield DateRange(startDate.get, endDate)
}

val route = RouteSpec().at(Get) bindTo Service.mk {
  req: Request =>
      range <--? req match {
        case Extracted(dates) => Ok(dates.startDate + " ->" + dates.endDate)
        case ExtractionFailed(sp) => BadRequest(sp.mkString(", "))
        case NotProvided => BadRequest()
      }
}
```

`Extractable` is modular, so instances can be embedded inside each other in for comprehensions to build object graphs from the 
incoming request.

The above example can be further simplified by use of the built-in `Filters.Request.ExtractableRequest` filter to transform the input:
```
  Filters.Request.ExtractableRequest(range).andThen(Service.mk[DateRange, Response] {
    dateRange => ...
  })
```

<a class="next" href="http://fintrospect.io/templating-and-static-content" target="_top"><button type="button" class="btn btn-sm btn-default">next: templating and static content</button></a>
