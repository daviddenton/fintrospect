Note that to avoid dependency bloat, Fintrospect only ships with the above JSON library bindings - you'll need to bring in the library of your choice as an additional dependency.

The simplest (least concise) way to invoke an auto-marshalling (ie. typesafe) ResponseBuilder is along the lines of:
```
val responseNoImplicits: Future[Response] = ResponseBuilder.toFuture(
Xml.ResponseBuilder.HttpResponse(Ok).withContent(<xml>lashings and lashings of wonderful</xml>)
)
```
... although with tiny bit of implicit magic (boo-hiss!), you can reduce this to the rather more concise:
```
import Xml.ResponseBuilder._
val responseViaImplicits: Future[Response] = Ok(<xml>lashings and lashings of wonderful</xml>)
```
