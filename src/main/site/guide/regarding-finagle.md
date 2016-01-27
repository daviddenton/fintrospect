# regarding finagle
Since Fintrospect is build on top of Finagle, it's worth acquainting yourself with it's concepts, which can be found here: http://twitter.github.io/finagle/guide

&tldr; version:
1. Finagle provides protocol-agnostic RPC and is based on Netty.
2. It is mainly asynchronous and makes heavy usage of Twitter's version of Scala Futures.
3. It defines identical Service and Filter interfaces for both client and server APIs that contain a single method:
```
Service:  def apply(request : Req) : com.twitter.util.Future[Rep]
Filter:   def apply(request : ReqIn, service : com.twitter.finagle.Service[ReqOut, RepIn]) : com.twitter.util.Future[RepOut]
```
where the types Req and Rep represent the Request and Response types for the protocol in question.

Note that in order to aid the reader, the code in this guide has omitted imports that would have made the it read more nicely. The sacrifices we make in the name of learning... :)

## broad concepts
Fintrospect is a library designed to facilitate painless definition, serving and consumption of HTTP APIs. It uses the following main concepts:
- RouteSpec: defines the overall HTTP contract of an endpoint. This contract can then be bound to a Finagle Service representing an HTTP client, or bundled into a Module and attached to a Finagle HTTP server.
- ParameterSpec: defines the acceptable format for a request parameter (Path/Query/Header/Form-field). Provides the auto-marshalling mechanic for serializing and deserializing objects to and from HTTP message.
- BodySpec: similar to ParameterSpec, but applied to the body of an HTTP message.
- ModuleSpec: defines a set of Routes which are grouped under a particular request path. These modules can be combined and then converted to a Finagle service and attached to a Finagle HTTP server. Each module provides an endpoint under which it's own runtime-generated documentation can be served (eg. in Swagger format).
