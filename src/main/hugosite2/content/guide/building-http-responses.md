+++
title = "building http responses"
weight = 5
+++

It's all very well being able to extract pieces of data from HTTP requests, but that's only half the story - we also want to be able to easily build responses. Fintrospect comes bundled with a extensible set of HTTP Response Builders to do this. The very simplest way is by using a ResponseBuilder object directly...
```
ResponseBuilder.toFuture(
    ResponseBuilder.HttpResponse(ContentTypes.APPLICATION_JSON).withCode(Status.Ok).withContent("some text").build()
)
```

However, this only handles Strings and Buffer types directly. Also bundled are a set of bindings which provide ResponseBuilders for 
handling content types like JSON or XML in a set of popular OSS libraries. These live in the ```io.fintrospect.formats``` package. Currently supported formats are in the table below:

INSERT LINK TO FORMAT TABLE HERE..

The simplest (least concise) way to invoke an auto-marshalling (ie. typesafe) ResponseBuilder is along the lines of:
```
Xml.ResponseBuilder.HttpResponse(Status.Ok).withContent(<xml>lashings and lashings of wonderful</xml>).toFuture
```
... although with tiny bit of implicit magic, we can use custom status methods on the builders and then convert the `ResponseBuilder` to 
a `Future[Response]`, you can reduce this to the rather more concise:
```
import io.fintrospect.formats.Xml.ResponseBuilder._
val responseViaImplicits: Future[Response] = Ok(<xml>lashings and lashings of wonderful</xml>)
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
import io.fintrospect.formats.Circe.ResponseBuilder._

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
import io.fintrospect.formats.Circe
import io.fintrospect.formats.Circe.Auto._

case class ReversedEmailAddress(sserdda: String)

val domainSvc = Service.mk[EmailAddress, ReversedEmailAddress] { 
    email => Future(ReversedEmailAddress(email.address.reverse)) 
}
val httpSvc: Service[Request, Response] = Circe.Auto.InOut(domainSvc)    
```

<a class="next" href="/guide/cross-field-validation"><button type="button" class="btn btn-sm btn-default">next: cross field validation</button></a>
