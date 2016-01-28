# building HTTP Responses
It's all very well being able to extract pieces of data from HTTP requests, but that's only half the story - we also want to be able to easily build responses. Fintrospect comes bundled with a extensible set of HTTP Response Builders to do this. The very simplest way is by using a ResponseBuilder object directly...
```
ResponseBuilder.toFuture(
    ResponseBuilder.HttpResponse(ContentTypes.APPLICATION_JSON).withCode(Ok).withContent("some text").build()
)
```
However, this only handles Strings and Buffer types directly. Also bundled are a set of bindings which provide ResponseBuilders for handling content types like JSON or XML in a set of popular OSS libraries. These live in the ```io.fintrospect.formats``` package. Currently supported formats are in the table below:
<table class="code table table-bordered">
<tr>
    <td>Library</td>
    <td>Content-Type</td>
    <td>Additional SBT deps</td>
    <td>Fintrospect class</td>
</tr>
<tr>
    <td>Argo</td>
    <td>application/json</td>
    <td>"net.sourceforge.argo" % "argo" % "3.12"</td>
    <td>io.fintrospect.formats.json.Argo</td>
</tr>
<tr>
    <td>Argonaut</td>
    <td>application/json</td>
    <td>"io.argonaut" %% "argonaut" % "6.0.4"</td>
    <td>io.fintrospect.formats.json.Argonaut</td>
</tr>
<tr>
    <td>Circe</td>
    <td>application/json</td>
    <td>"io.circe" %% "circe-core" % "0.2.1"<br/>"io.circe" %% "circe-parse" % "0.2.1"<br/>"io.circe" %% "circe-generic" % "0.2.1" </td>
    <td>io.fintrospect.formats.json.Circe</td>
</tr>
<tr>
    <td>GSON</td>
    <td>application/json</td>
    <td>"com.google.code.gson" % "gson" % "2.5"</td>
    <td>io.fintrospect.formats.json.Gson</td>
</tr>
<tr>
    <td>HTML</td>
    <td>text/html</td>
    <td>-</td>
    <td>io.fintrospect.formats.Html</td>
</tr>
<tr>
    <td>Json4S Native</td>
    <td>application/json</td>
    <td>"org.json4s" %% "json4s-native" % "3.3.0"</td>
    <td>io.fintrospect.formats.json.Json.Native<br/>io.fintrospect.formats.json.Json.NativeDoubleMode</td>
</tr>
<tr>
    <td>Json4S Jackson</td>
    <td>application/json</td>
    <td>"org.json4s" %% "json4s-jackson" % "3.3.0"</td>
    <td>io.fintrospect.formats.json.Json.Jackson<br/>io.fintrospect.formats.json.Json.JacksonDoubleMode</td>
</tr>
<tr>
    <td>Plain Text</td>
    <td>text/plain</td>
    <td>-</td>
    <td>io.fintrospect.formats.PlainText</td>
</tr>
<tr>
    <td>Play</td>
    <td>application/json</td>
    <td>"com.typesafe.play" %% "play-json" % "2.4.3"</td>
    <td>io.fintrospect.formats.json.Play</td>
</tr>
<tr>
    <td>Spray</td>
    <td>application/json</td>
    <td>"io.spray" %% "spray-json" % "1.3.2"</td>
    <td>io.fintrospect.formats.json.Spray</td>
</tr>
<tr>
    <td>(XHTML)</td>
    <td>application/xhtml+xml</td>
    <td>-</td>
    <td>io.fintrospect.formats.XHtml</td>
</tr>
<tr>
    <td>(XML)</td>
    <td>application/xml</td>
    <td>-</td>
    <td>io.fintrospect.formats.Xml</td>
</tr>
</table>

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
