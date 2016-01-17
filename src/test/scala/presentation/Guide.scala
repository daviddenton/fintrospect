package presentation

import java.time.LocalDate

import com.twitter.finagle.http.Method.Get
import com.twitter.finagle.http.{Method, Request, Response, Status}
import com.twitter.finagle.{Http, Service}
import com.twitter.util.Future
import io.fintrospect.parameters._
import io.fintrospect.{ContentTypes, RouteClient, RouteSpec}

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
    The types Req and Rep represent the Request and Response types for the protocol in question.

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
    .returning(Status.Created -> "Employee was created")
    .returning(Status.Conflict -> "Employee already exists")
    .at(Method.Post) / "user" / Path.integer("departmentId")

  /*
  ##Defining request parameters and bodies
  As can be seen above, request parameters are created in a uniform way using the standardised objects Path,
  Header, Query, FormField and Body. The general form for definition is:

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
  A RouteSpec needs to be bound to a standard Finagle Service to receive requests. Since these are very lightweight,
  we create a new instance of the Service for every request, and bind the RouteSpec to a factory method which receives
  the dynamic Path parameters and returns the Service. Other parameters can be retrieved directly in a typesafe manner from
  the HTTP request by using <--() or from() method on the parameter declaration.

  Note that the validity of ALL parameters which are attached to a RouteSpec is verified by Fintrospect before requests
  make it to these bound Services, so you do not need to worry about implementing any validation at this point.
   */
  val holidays = Query.required.*.localDate("datesTakenAsHoliday")
  val includeManagement = Header.optional.boolean("includeManagement")

  def findUsersOnHoliday(departmentId: Integer) = new Service[Request, Response] {
    override def apply(request: Request): Future[Response] = {
      val holidayDates: Seq[LocalDate] = holidays <-- request
      val includeManagementFlag: Option[Boolean] = includeManagement <-- request
      val response = Response(Status.Ok)
      val baseMsg = s"Everyone from department $departmentId was at work on $holidayDates"
      response.contentString = baseMsg + (if(includeManagementFlag.getOrElse(false)) "" else ", even the management")
      Future.value(response)
    }
  }

  RouteSpec().taking(holidays).taking(includeManagement).at(Method.Get) / "employee" / Path.integer("departmentId") bindTo findUsersOnHoliday

  /*
  ###Modules
  A Module is a collection of Routes that share a common root context. Modules can be combined with one another and
  ultimately converted into a Finagle Service object which is then attached in the normal way to an HTTP server.
  */

  /*
  ###Clientside
  A RouteSpec can also be bound to a standard Finagle HTTP client and then called as a function, passing in the parameters
  which are bound to values by using the -->() or of() method. The client marshalls the passed parameters into an HTTP
  request and returns a Twitter Future containing the response. Any required manipulation of the Request (such as adding
   timeouts or caching headers) can be done in the standard way chaining Filters to the Finagle HTTP client:
 */

  val employeeId = Path.integer("employeeId")
  val name = Query.required.string("name")
  val client: RouteClient = RouteSpec().taking(name).at(Get) / "employee" / employeeId bindToClient Http.newService("localhost:10000")

  val theCall: Future[Response] = client(employeeId --> 1, name --> "")


  //
  //  Body
  //  Form
  //  Json
  //  Custom
  //
  //  Servers
  //  Module
  //  Parameter Validation
  //    Security
  //
  //  API Documentation
  //    Swagger
  //  Custom
  //
  //  Responses
  //  Builders
  //
  //  JSON Formats
  //    Json4S
  //  binding
  //  custom
  //  Custom
  //
  //  Clients
  //  Creating
  //  Binding parameters
  //    Advanced re-usage
  //
  //
  //  Using this library, you can:
  //    - Define individual HTTP routes and compose them into sensible context-based modules.
  //  - Declare both required and optional parameters to be used in the following locations: ```Path/Header/Query/Form/Body```. Retrieval of the parameters is simple and type-safe (```[T]``` for required, ```Option[T]``` for optional). Custom datatypes
  //  for parameters are supported. Also support for typesafe conversions of custom types.
  //    - Automatically generate documentation in a variety of formats (e.g. <a href="http://swagger.io/" target="_top">Swagger</a> v1.1 and v2.0). Pluggable architecture for adding your own format renderers (e.g other JSON, XML).
  //    - Endpoints automatically verify the presence and validity of both optional and required parameters. If any parameters are missing or invalid, a ```BAD_REQUEST``` response is generated - meaning that no extra validation code is required for these parameters in your controller code.
  //  - The library provide identification HTTP headers for dynamic-path based endpoints, removing all dynamic path elements. This allows, for example, calls to particular endpoints to be grouped for metric purposes. e.g. ```/search/author/rowling -> /search/author/{name}```.
  //    - Define HTTP Client endpoints APIs which reuse the same syntax and parameter bindings as the server-side, which means that you can use the same route specification to define both sides of the transport boundary. This allows, for example,
  //  the HTTP API of a downstream servers (for a fake) to be created with no effort, and exactly matching the client side. These endpoints are also exposed as simple functions.
  //  - A set of HTTP Response builders with pluggable extension points for custom formats; currently supported are:


}
