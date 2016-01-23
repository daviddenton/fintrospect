package presentation

import java.net.URL
import java.time.LocalDate

import com.twitter.finagle.http.Method.Get
import com.twitter.finagle.http.Status._
import com.twitter.finagle.http.path.Root
import com.twitter.finagle.http.{Method, Request, Response}
import com.twitter.finagle.{Http, Service}
import com.twitter.util.Future
import io.fintrospect._
import io.fintrospect.formats.{Html, ResponseBuilder, Xml}
import io.fintrospect.parameters._
import io.fintrospect.renderers.SiteMapModuleRenderer
import io.fintrospect.renderers.swagger2dot0.{ApiInfo, Swagger2dot0Json}
import io.fintrospect.templating.{RenderMustacheView, View}

import scala.language.reflectiveCalls

object Guide {
  /*
    ##Pre-amble
    Since Fintrospect is build on top of Finagle, it's worth acquainting yourself with it's concepts, which
    can be found here: http://twitter.github.io/finagle/guide

    &tldr; version:
    1. Finagle provides protocol-agnostic RPC and is based on Netty.
    2. It is mainly asynchronous and makes heavy usage of Twitter's version of Scala Futures.
    3. It defines identical Service and Filter interfaces for both client and server APIs that contain a single method:
      Service:  def apply(request : Req) : com.twitter.util.Future[Rep]
      Filter:   def apply(request : ReqIn, service : com.twitter.finagle.Service[ReqOut, RepIn]) : com.twitter.util.Future[RepOut]
    where the types Req and Rep represent the Request and Response types for the protocol in question.

    Note that in order to aid the reader, the code in this guide has omitted imports that would have made the it read
    more nicely. The sacrifices we make in the name of learning... :)

    ##Broad concepts
    Fintrospect is a library designed to facilitate painless definition, serving and consumption of HTTP APIs.
    It uses the following main concepts:

    - RouteSpec: defines the overall HTTP contract of an endpoint. This contract can then be bound to a Finagle
    Service representing an HTTP client, or bundled into a Module and attached to a Finagle HTTP server.
    - ParameterSpec: defines the acceptable format for a request parameter (Path/Query/Header/Form-field). Provides
    the auto-marshalling mechanic for serializing and deserializing objects to and from HTTP message.
    - BodySpec: similar to ParameterSpec, but applied to the body of an HTTP message.
    - ModuleSpec: defines a set of Routes which are grouped under a particular request path. These modules can be
    combined and then converted to a Finagle service and attached to a Finagle HTTP server. Each module provides an
    endpoint under which it's own runtime-generated documentation can be served (eg. in Swagger format).

    ##Defining routes
    A RouteSpec object defines the specification of the contract and the API follows the immutable builder pattern.
    Apart from the path elements (which terminate the builder), all of the "builder-y" calls here are optional, as are
    the descriptive strings (we'll see how they are used later). Here's the simplest possible REST-like example for
    getting all employees in a system:
    */
  RouteSpec("list all employees").at(Method.Get) / "employee"

  /*
  Notice that the request in that  example was completely static? If we want an example of a dynamic endpoint, such as
  listing all users in a particular numerically-identified department, then we can introduce a Path parameter:
   */
  RouteSpec("list all employees in a particular group").at(Method.Get) / "employee" / Path.integer("departmentId")

  /*
  ... and we can do the same for Header and Query parameters; both optional and mandatory parameters are supported,
  as are parameters that can appear multiple times.:
     */
  RouteSpec("list all employees in a particular group")
    .taking(Header.optional.boolean("listOnlyActive"))
    .taking(Query.required.*.localDate("datesTakenAsHoliday"))
    .at(Method.Get) / "employee" / Path.integer("departmentId")

  /*
  Moving onto HTTP bodies - for example adding an employee via a HTTP Post and declaring the content types that
  we produce (although this is optional):
   */
  RouteSpec("add employee", "Insert a new employee, failing if it already exists")
    .producing(ContentTypes.TEXT_PLAIN)
    .body(Body.form(FormField.required.string("name"), FormField.required.localDate("dateOfBirth")))
    .at(Method.Post) / "user" / Path.integer("departmentId")

  /*
  ... or via a form submission and declaring possible responses:
   */
  RouteSpec("add user", "Insert a new employee, failing if it already exists")
    .body(Body.form(FormField.required.string("name"), FormField.required.localDate("dateOfBirth")))
    .returning(Created -> "Employee was created")
    .returning(Conflict -> "Employee already exists")
    .at(Method.Post) / "user" / Path.integer("departmentId")

  /*
  ##Defining request parameters and bodies
  As can be seen above, request parameters are created in a uniform way using the standardised objects Path, Header, Query,
  FormField and Body. The general form for definition is:

  <parameter location>.<required|optional>.<param type>("name")

  Since Path and Body parameters are always required, the middle step is omitted from this form for these types.

  There are convenience methods for a standard set of "primitive" types, plus extensions for other common formats
  such as native Scala XML, Forms (body only) and JSON (more on this later).

  ##Custom formats
  These can be implemented by defining a ParameterSpec or BodySpec and passing this in instead of calling the
  <param type> method in the form above. These Spec objects define the serialization and deserialization mechanisms
  from the String format that comes in on the request. An example for a simple domain case class Birthday:
   */
  case class Birthday(value: LocalDate) {
    override def toString = value.toString
  }

  object Birthday {
    def from(s: String) = Birthday(LocalDate.parse(s))
  }

  val birthdayAsAQueryParam = Query.required(ParameterSpec[Birthday]("DOB", None, StringParamType, Birthday.from, _.toString))

  val birthdayAsABody = Body(BodySpec[Birthday](Option("DOB"), ContentTypes.TEXT_PLAIN, Birthday.from, _.toString))

  /*
  Note that in the above we are only concerned with the happy case on-the-wire values. The serialize and deserialize
  methods should throw exceptions if unsuccessful - these are caught by the request validation mechanism and turned into
  a rejected BadRequest (400) response which is returned to the caller.
   */

  /*
  ##Using routes
  Once the RouteSpec has been defined, it can be bound to either an HTTP Endpoint or to an HTTP Client.

  ###Serverside
  A RouteSpec needs to be bound to a standard Finagle Service to receive requests. Since these are very lightweight, we create
  a new instance of the Service for every request, and bind the RouteSpec to a factory method which receivesthe dynamic Path
  parameters and returns the Service. Other parameters can be retrieved directly in a typesafe manner from the HTTP request by
  using <--() or from() method on the parameter declaration.
  Note that the validity of ALL parameters which are attached to a RouteSpec is verified by Fintrospect before requests
  make it to these bound Services, so you do not need to worry about implementing any validation at this point.
   */
  val holidays = Query.required.*.localDate("datesTakenAsHoliday")
  val includeManagement = Header.optional.boolean("includeManagement")

  def findEmployeesOnHoliday(departmentId: Integer) = Service.mk[Request, Response] {
    request =>
      val holidayDates: Seq[LocalDate] = holidays <-- request
      val includeManagementFlag: Option[Boolean] = includeManagement <-- request
      val response = Response(Ok)
      val baseMsg = s"Everyone from department $departmentId was at work on $holidayDates"
      response.contentString = baseMsg + (if (includeManagementFlag.getOrElse(false)) "" else ", even the management")
      Future.value(response)
  }

  RouteSpec().taking(holidays).taking(includeManagement).at(Method.Get) / "employee" / Path.integer("departmentId") bindTo findEmployeesOnHoliday

  /*
  ###Modules
  A Module is a collection of Routes that share a common root URL context. Add the routes and then convert into a standard Finagle
  Service object which is then attached in the normal way to an HTTP server.
  */
  def listEmployees(): Service[Request, Response] = Service.mk(req => Future.value(Response()))

  Http.serve(":8080",
    ModuleSpec(Root / "employee")
      .withRoute(RouteSpec("lists all employees").at(Method.Get) bindTo listEmployees)
      .toService
  )

  /*
  Modules with different root contexts can also be combined with one another and then converted to a Service:
   */
  Module.toService(ModuleSpec(Root / "a").combine(ModuleSpec(Root / "b")))

  /*
  ####Self-describing Module APIs
  A big feature of the Fintrospect library is the ability to generate API documentation at runtime. This can be activated
  by passing in a ModuleRenderer implementation when creating the ModuleSpec and when this is done, a new endpoint is created
  at the root of the module context (overridable) which serves this documentation. Bundled with Fintrospect are Swagger (1.1
  and 2.0) JSON and a simple JSON format. Other implementations are pluggable by implementing a Trait - see the example code
  for a simple XML implementation.
   */
  ModuleSpec(Root / "employee", Swagger2dot0Json(ApiInfo("an employee discovery API", "3.0")))

  /*
  ####Security
  Module routes can secured by adding an implementation of the Security trait - this essentially provides a filter through
  which all requests will be passed. An ApiKey implementation is bundled with the library which return an unauthorized HTTP
  response code when a request does not pass authentication.
  */
  ModuleSpec(Root / "employee").securedBy(ApiKey(Header.required.string("api_key"), (key: String) => Future.value(key == "extremelySecretThing")))

  /*
  ###Clientside
  A RouteSpec can also be bound to a standard Finagle HTTP client and then called as a function, passing in the parameters
  which are bound to values by using the -->() or of() method. The client marshalls the passed parameters into an HTTP
  request and returns a Twitter Future containing the response. Any required manipulation of the Request (such as adding
   timeouts or caching headers) can be done in the standard way by chaining Filters to the Finagle HTTP client:
    */
  val employeeId = Path.integer("employeeId")
  val name = Query.required.string("name")
  val client: RouteClient = RouteSpec().taking(name).at(Get) / "employee" / employeeId bindToClient Http.newService("localhost:10000")
  val response: Future[Response] = client(employeeId --> 1, name --> "")

  /*
  ###Super-cool feature time: Auto-generation of Fake HTTP contracts
  Because the RouteSpec objects can be used to bind to either a Server OR a Client, we can be spectacularly smug and use
  them on BOTH sides of an HTTP boundary to provide a typesafe remote contract, to either:
  1. Auto-generate fake HTTP server implementations for remote HTTP dependencies. In this case, defining the RouteSpecs
  as part of an HTTP client contract and then simply reusing them as the server-side contract of a testing fake - see the
  "full" example code for - erm - an example! :)
  2. Use a shared library approach to define a contract and the data objects that go across it for reuse in multiple applications,
  each of which import the shared library. This obviously binary-couples the applications together to a certain degree, so utmost
  care should be taken, backed up with sufficient CDC-style testing to ensure that the version of the contract deployed is valid
  on both ends.
   */


  /*
  ##Building HTTP Responses
  It's all very well being able to extract pieces of data from HTTP requests, but that's only half the story - we also want to be
  able to easily build responses. Fintrospect comes bundled with a extensible set of HTTP Response Builders to do this. The very
  simplest way is by using a ResponseBuilder object directly...
  */
  ResponseBuilder.toFuture(
    ResponseBuilder.HttpResponse(ContentTypes.APPLICATION_JSON).withCode(Ok).withContent("some text").build()
  )

  /*
  However, this only handles Strings and Buffer types directly. Also bundled are a set of bindings which provide ResponseBuilders for
  handling content types like JSON or XML in a set of popular OSS libraries. These live in the io.fintrospect.formats package. Currently
  supported formats are in the table below:

  <table border="1px">
    <tr>
        <td>Library</td>
        <td>Content-Type</td>
        <td>Additional SBT depdendencies</td>
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

  Note that to avoid dependency bloat, Fintrospect only ships with the above JSON library bindings - you'll need to bring in the
  library of your choice as an additional dependency.

  The simplest (least concise) way to invoke an auto-marshalling (ie. typesafe) ResponseBuilder is along the lines of:
  */
  val responseNoImplicits: Future[Response] = ResponseBuilder.toFuture(
    Xml.ResponseBuilder.HttpResponse(Ok).withContent(<xml>
      lashings and lashings of wonderful
    </xml>)
  )

  /*
  ... although with tiny bit of implicit magic (boo-hiss!), you can reduce this to the rather more concise:
  */

  import Xml.ResponseBuilder._

  val responseViaImplicits: Future[Response] = Ok(<xml>lashings and lashings of wonderful</xml>)

  /*
    *   ##Templating
    *   Templates are applied by using custom Filters to convert View instances into standard Http Responses. Simply implement the View trait and then
    *   put a matching template file onto the classpath, and chain the output of the model-creating Service into the Filter. You can do this for entire
    *   modules by making the ModuleSpec itself generified on View and using the templating Filter as a Module-level filter:
    */
  case class ViewMessage(value: String) extends View

  def showMessage() = Service.mk[Request, View] { _ => Future.value(ViewMessage("some value to be displayed")) }

  val webModule = ModuleSpec[View](Root / "web",
    new SiteMapModuleRenderer(new URL("http://root.com")),
    new RenderMustacheView(Html.ResponseBuilder))
    .withRoute(RouteSpec().at(Get) / "message" bindTo showMessage)

  /*
  Similarly to how the ResponseBuilders work, no 3rd-party dependencies are bundled with Fintrospect - simply import the extra SBT dependencies as required:
  <table border="1px">
  <tr>
      <td>Library</td>
      <td>Template filename suffix</td>
      <td>Additional SBT depdendencies</td>
      <td>Filter class</td>
  </tr>
  <tr>
      <td>Handlebars</td>
      <td>.hbs</td>
      <td>"com.gilt" %% "handlebars-scala" % "2.0.1"</td>
      <td>io.fintrospect.templating.RenderHandlebarsView</td>
  </tr>
  <tr>
      <td>Mustache (v2.11 only)</td>
      <td>.mustache</td>
      <td>"com.github.spullara.mustache.java" % "compiler" % "0.9.1"<br/>"com.github.spullara.mustache.java" % "scala-extensions-2.11" % "0.9.1"</td>
      <td>io.fintrospect.templating.RenderMustacheView</td>
  </tr>
  </table>
  *
    *   ##Static content
    *   Files can be served easily by using a StaticModule:
    */
  val publicModule = StaticModule(Root / "public", "public")



}
