package presentation

import java.time.LocalDate

import com.twitter.finagle.Service
import com.twitter.finagle.http.{Method, Request, Response, Status}
import com.twitter.util.Future
import io.fintrospect.parameters._
import io.fintrospect.{ContentTypes, RouteSpec}

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
    Fintrospect is a library designed to facilitate painless definition of serving and consumption of HTTP APIs.
    It uses the following main concepts:

    - RouteSpec: defines the overall HTTP contract of an endpoint. This contract can then be bound to a Finagle
    Service representing an HTTP client, or bundled into a Module and attached to a Finagle HTTP server.
    - ParameterSpec: defines the acceptable formats for request parameters (Path/Query/Header/Form-field). Provides
    the auto-marshalling mechanic for serializing and deserializing objects to and from HTTP message.
    - BodySpec: similar to ParameterSpec, but applied to the body of an HTTP message.
    - ModuleSpec: defines a set of Routes which are grouped under a particular request path. These modules can be
    combined and then converted to a Finagle service and attached to a Finagle HTTP server. Each module provides an
    endpoint under which it's own runtime-generated documentation can be served (eg. in Swagger format).

    ##Defining routes
    A RouteSpec object defines the specification of the contract and the API follows the immutable builder pattern.
    Apart from the path elements (which terminate the builder), all of the "builder-y" calls here are optional, as are
    the descriptive strings (we'll see how they are used later). Here's the simplest possible RESTy example for
    getting all users in a system:
    */
  RouteSpec("list all users").at(Method.Get) / "user"

  /*
  Notice that example was completely static? If we want an example of a dynamic endpoint, such as listing all users in
  a particular numerically-identified group, then we need to introduce a Path parameter:
   */
  RouteSpec("list all users in a particular group").at(Method.Get) / "user" / Path.integer("groupId")

  /*
  ... and we can do the same for Header and Query parameters; both optional and mandatory parameters are supported,
  as are parameters that can appear multiple times:
     */
  RouteSpec("list all users in a particular group")
    .taking(Header.optional.boolean("listOnlyActive"))
    .taking(Query.required.*.localDate("birthdayDate"))
    .at(Method.Get) / "user" / Path.integer("groupId")

  /*
  Moving onto HTTP bodies- for example adding a user via a HTTP Post and declaring the content types that
  we produce (although this is optional):
   */
  RouteSpec("add user", "Insert a new user, failing if it already exists")
    .producing(ContentTypes.TEXT_PLAIN)
    .body(Body.form(FormField.required.string("name"), FormField.required.localDate("dateOfBirth")))
    .at(Method.Post) / "user" / Path.integer("groupId")

  /*
  ... or via a form submission and declaring possible responses:
   */
  RouteSpec("add user", "Insert a new user, failing if it already exists")
    .body(Body.form(FormField.required.string("name"), FormField.required.localDate("dateOfBirth")))
    .returning(Status.Created -> "User was created")
    .returning(Status.Conflict -> "User already exists")
    .at(Method.Post) / "user" / Path.integer("groupId")

  /*
  ##Defining request parameters and bodies
  As can be seen above, request parameters are created in a uniform way using the standardised objects Path,
  Header, Query, FormField and Body. The general form for definition is:

  <parameter location>.<required|optional>.<param type>("name")

  Since Path and Body parameters are always required, the middle step is omitted from this form.

  There are convenience methods for a standard set of "primitive" types, plus extensions for other common formats
  such as native Scala XML, Forms (body only) and JSON (more on this later).

  ##Custom formats
  These can be implemented by defining a ParameterSpec or BodySpec and passing this in instead of calling the
  <param type method> in the form above. These Spec objects define the serialization and deserialization mechanisms
  from the String format that comes in on the wire. An example for a simple domain case class Birthday:
   */
  case class Birthday(value: LocalDate) {
    override def toString = value.toString
  }

  object Birthday {
    def from(s: String) = Birthday(LocalDate.parse(s))
  }

  val birthdayAsAQueryParam = Query.required(
    ParameterSpec[Birthday]("DOB", None, StringParamType, Birthday.from, _.toString)
  )

  val birthdayAsABody = Body(BodySpec[Birthday](Option("DOB"), ContentTypes.TEXT_PLAIN, Birthday.from, _.toString))

  /*
  Note that in the above we are only concerned with the happy case on-the-wire values. The serialize and deserialize
  methods should blow up if unsuccessful.
   */

  /*
  ##Using routes
  Once the RouteSpec has been defined, it can be bound to either as an HTTP Endpoint or in a Client.

  ##Serverside
  Once a RouteSpec is defined, it needs to be bound to a standard Finagle Service to receive requests. Since
  these are very lightweight we create a new instance of the Service for every request, and bind the RouteSpec
  to a factory method which receives the dynamic Path parameters and returns the Service. Taking the earlier
  example of looking up Users by numeric Group ID:
  */

  def listUsersForGroup(groupId: Int) = new Service[Request, Response] {
    override def apply(request: Request): Future[Response] = Future.value(Response())
  }

  RouteSpec().at(Method.Get) / "user" / Path.int("groupId") bindTo listUsersForGroup

  /*
  Parameter retrieval
   */

  /*
    ##Defining Route endpoints


      RouteSpec
      Parameters
      ParameterSpec
      Custom
     */
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
