package presentation

import com.twitter.finagle.http.Method
import io.fintrospect.RouteSpec

object Guide {
  /*
    ##Pre-requisites
    Since Fintrospect is build on top of Finagle, it's worth acquainting yourself with it;s broad concepts, which
    can be found here: http://twitter.github.io/finagle/guide

    &tldr; version:
    1. Finagle provides protocol-agnostic RPC and is based on Netty.
    2. It is mainly asynchronous and makes heavy usage of Twitter's version of Scala Futures.
    3. It defines identical Service and Filter interfaces for both client and server APIs that contain a single method:
      Service:  def apply(request : Req) : com.twitter.util.Future[Rep]
      Filter:   def apply(request : ReqIn, service : com.twitter.finagle.Service[ReqOut, RepIn]) : com.twitter.util.Future[RepOut]
    The types Req and Rep represent the Request and Response types for the protocol in question.

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

    ##Routes
    A RouteSpec object defines the specification of the contract. The simplest and most boring example is:
   */
  val myRoute = RouteSpec().at(Method.Get) / "endpoint"
  /*

  val route = RouteSpec("search for books")
    .taking(maxPages)
    .body(form)
    .returning(Ok -> "we found your book", array(Book("a book", "authorName", 99).toJson))
    .returning(BadRequest -> "invalid request")
    .producing(APPLICATION_JSON)
    .at(Post) / "search" bindTo search
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
