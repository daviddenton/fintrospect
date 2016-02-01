Note that to avoid dependency bloat, Fintrospect only ships with the above JSON library bindings - you'll need to bring in the library of your choice as an additional dependency.

The simplest (least concise) way to invoke an auto-marshalling (ie. type-safe) ResponseBuilder is along the lines of:
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
<a class="next" href="http://fintrospect.io/templating-and-static-content" target="_top"><button type="button" class="btn btn-sm btn-default">next: templating and static content</button></a>
