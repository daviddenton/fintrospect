The simplest (least concise) way to invoke an auto-marshalling (ie. typesafe) ResponseBuilder is along the lines of:
```
val responseNoImplicits: Future[Response] = ResponseBuilder.toFuture(
Xml.ResponseBuilder.HttpResponse(Status.Ok).withContent(<xml>lashings and lashings of wonderful</xml>)
)
```
... although with tiny bit of implicit magic to convert the `Status` object to a `ResponseBuilder` and then convert the `ResponseBuilder` to 
a `Future[Response]`, you can reduce this to the rather more concise:
```
import io.fintrospect.formats.Xml.ResponseBuilder.implicits._
val responseViaImplicits: Future[Response] = Status.Ok(<xml>lashings and lashings of wonderful</xml>)
```

These ResponseBuilders also support `AsyncStream[T]`, so you can build services which can stream responses in a typesafe way.

## taking advantage of auto-marshalling
### controlled mode
Some of the JSON libraries (`Circe`, `Argonaut`, `Json4S`, `Play`) supported by Fintrospect support auto-marshalling of Scala Case 
class instances directly to JSON without any custom conversion code needing to be written. This is supported by `encode()` and `decode()` 
methods present on the relevant Fintrospect `JsonFormat` format instance (e.g. `io.fintrospect.formats.Circe.JsonFormat`). Generally, 
 these are very simple to use:
```
case class EmailAddress(address: String)

import io.circe.generic.auto._
import io.fintrospect.formats.Circe.ResponseBuilder.implicits._

Status.Ok(Circe.JsonFormat.encode(EmailAddress("dev@fintrospect.io")
```
The auto-marshalling functionality of these JSON libraries requires implicit parameters in order to make it work. This requirement is 
echoed in the signatures of the relevant Fintrospect `encode()` and `decode()` methods, but unless you're going to be providing custom 
encoder/decoder instances, you can get away with just importing the relevant `implicit` params from the parent lib, as in the example above.

### full-auto mode
Fintrospect also contains filters which allow you to abstract away the HTTP Request/Response entirely. In this example, 
the `Circe.Filters.AutoInOut` filter converts the `Service[Request, Response]` to a `Service[EmailAddress, ReversedEmailAddress]`, auto-converting 
the case class objects in and out of the request/response. The returned status code in the `Response` is 200, but this is overridable:
```
import io.circe.generic.auto._

case class ReversedEmailAddress(sserdda: String)

val domainSvc = Service.mk[EmailAddress, ReversedEmailAddress] { 
    email => Future.value(ReversedEmailAddress(email.address.reverse)) 
}
val httpSvc: Service[Request, Response] = Circe.Filters.AutoInOut(domainSvc)    
```

<a class="next" href="http://fintrospect.io/cross-field-validation"><button type="button" class="btn btn-sm btn-default">next: cross field validation</button></a>
