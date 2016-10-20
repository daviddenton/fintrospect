<h1 class="githubonly">Roadmap/Release Notes/Migration Guide</h1>

The main API is stable, but expect some amount of breaking changes around major releases as new features are added. All breaking changes are documented with a migration step where possible.

## Backlog
- Add strict `Content-Type` header checking for `RouteSpec`s based on `consuming()` and `withBody()` settings. Option for non-strictness.
- Add strict `Accept` header checking for `RouteSpec`s based on `producing()` settings. Option for non-strictness.
- Add integrated self-hosted Swagger UI/ReDoc UI module

## 13.10.1
- Replaced `Circe.JsonFormat.patchBodySpec()` with `patchBody()` to ease use of Patch endpoint creations.

## 13.10.0
- Upgrade to various dependency versions, including Circe `0.5.4`.
- `Circe.JsonFormat`: Added explicit `patcher()` method to allow patch updates for case class instances (via auto-marshalling JSON modules).
- Added support to `StrictContentTypeNegotiation` for routes serving multiple content types. See `StrictMultiContentTypeRoute` example. 

## 13.9.1
- Bugfix: Issue #24. Remove transitive dependency on `sl4j-simple` in `fintrospect-handlebars` to avoid SL4J warnings.

## 13.9.0
- Breaking: Repackage of JSON message format libraries. `io.fintrospect.formats.json.<LibraryName>` are now just `io.fintrospect.formats` instead. To fix, simply find/replace the package names in your source.
- Breaking: Split `Json4s.Native` and `Json4s.Jackson` into their own top-level message objects. The mapping is `Json4s.Native` -> `Json4s`/`Json4sDoubleMode` and `Json4s.Jackson` -> `Json4sJackson`/`Json4sJacksonDoubleMode`, so simply find/replace references in your source.
- Upgrade to various dependency versions, including Finagle `6.38.0` and Circe `0.5.2`.

## 13.8.1
- Bugfix: Issue #23. On invalid request, AutoFilters blow up instead of producing BadRequest response

## 13.8.0
- Added MsgPack library support. Import new module `fintrospect-msgpack` to activate this support.

## 13.7.0
- Upgrade to various dependency versions, including Finagle `6.37.0` and Circe `0.5.1`.

## 13.6.0
- (Possible) Breaking: Refactored `StaticModule` to allow serving of directory-based assets. Introduced `Classpath` and `Directory` `ResourceLoaders` to allow for this change.

## 13.5.2 
- Addition of `Contract` and `ContractProxyModule`. This (optional) formalisation of client contracts allows simple
exposing of a client API contract via Swagger. See `ContractProxyExample`.
- (Unlikely) Break: Renamed of `IncompletePath` to `UnboundRoute` for clarity. This should be transparent to end users.
- Refactor of `Parameter` types and traits. This should be transparent to end users.
- Removal of Generics in `RequestFilters`. Should make code using these filters easier - by just removing Generics.

## 13.5.1
- Please ignore this release and roll back to 13.4.0.

## 13.4.0
- Upgrade to Finagle `6.36.0`
- Fixed site API docs
- Added `Body.webform()`, which allows errors to be collected for feedback to the user. See `formvalidation` example
- Breaking: Removed `NotProvided` extraction option. Extraction now exposes an `Option` instead
- Breaking: Renamed `InvalidParameter` to `ExtractionError`
- Breaking: Repackage of some classes from `io.fintrospect.parameters` to `io.fintrospect` and `io.fintrospect.util`

## 13.3.0
- Option for empty `string` validation to be rejected as `InvalidParameter` in `QueryParameters` et al.
- Breaking: removed unused `HttpRequestResponseUtil:contentFrom()` method. Simply use `request.contentString` instead
- `ResponseFilter.ReportingRouteLatency()` now blocks for reporting latency, as using onSuccess was not accurately reporting.

## 13.2.1
- Dynamic reloading of templates. See implementations of `Templates` (Mustache and Handlebars), or `templating` example.
- Breaking; Combined `RenderMustacheView` and `RenderHandlebarsView` into `RenderView`.

## 13.1.0
- Upgrade of some library dependency versions (`scala-xml` and `scala-parser-combinators`)

## 13.0.0
- v13! Unlucky for some (but hopefully not for us!)
- Dropped cross build to Scala 2.10. This is forced since `finagle-http` is dropping it.
- Breaking: Removal of all previously deprecated items in: `ResponseBuilder`, `Filters` and`FintrospectModule`. See below for notes on replacement.
- Breaking: With this release, we have repackaged Fintrospect into a core module `fintrospect-core` and a set of add-on modules (`fintrospect-circe`, `fintrospect-mustache` etc.). 
The artifacts also now live in a new Maven group: `io.fintrospect`. The module dependencies have been internalised, so various modules have explicitly internalised their dependencies.
Please read the <a href="http://fintrospect.io/installation">installation guide</a> for the new mechanism, but as a simple example, if previously your dependencies for fintrospect were:

```scala
libraryDependencies ++= Seq(
"com.twitter" %% "finagle-http" % "6.35.0",
"io.github.daviddenton" %% "fintrospect" % "12.21.0"
"io.circe" %% "circe-generic" % "0.4.1")
```
... then you now rely on the `fintrospect-core` and `fintrospect-circe` modules. Your dependencies should now be:
```scala
libraryDependencies ++= Seq(
"io.fintrospect" %% "fintrospect-core" % "13.0.0",
"io.fintrospect" %% "fintrospect-circe" % "13.0.0")
```

## 12.21.1
- Bugfix: Issue #21 Form extraction fails on empty fields

## 12.21.0
- Added `Validator` to provide error collection.
- Ability to map `ParameterSpec`s. This comes in useful when declaring ParameterSpecs of `AnyVal` wrapper classes. 
- (Small) Break: repackage of filter classes `io.fintrospect.util` -> `io.fintrospect.filters`. Renames are: `Filters.Request` -> `RequestFilters`, `Filters.Response` -> `ResponseFilters`
- (Small) Break: `Extractable` renamed to `Extractor`

## 12.20.1
- Bugfix: Issue #20 Static Module now resolves to index.html for default paths

## 12.20.0
- Convenience methods added to Extraction.

## 12.19.0
- Tidied up Extractables to finalise (again).

## 12.18.2
- Bugfix: Extractables short-circuiting incorrectly when last parameter is optional (issue #19).

## 12.18.1
- Bugfix: Extractables must return an Option to not shortcircuit in valid situations (issue #18).

## 12.18.0
- Facility to override body and parameter validation in `RouteSpec` - pass in a different `RequestValidation` instance
- Finalised extraction logic, including embedding of `Extractable`s

## 12.17.0
- More experimental extraction logic, including rename of `Validatable` -> `Extractable`

## 12.16.0
- Added utility filters
- More work on composites and extraction

## 12.15.0
- (Small) breaking change: Added reasons to invalid parameter validation. This means that the `badRequest()` method of `ModuleRenderer` implementations need to 
change to also take the `Seq(InvalidParameter)` instead of just `Seq[Parameter]`. Easily fix by inserting the first line of `val params = invalidParams.map(_.parameter)` 
into your `badRequest()` if you want to ignore the reason.
- Massive tidy-up of Extraction logic + introduction of experimental support for cross-parameter validation via for comprehensions

## v12.14.1
- Bugfix: Empty forms can be retrieved from a request, even when they have required fields (issue #15).
- Bugfix: Cache filters should only apply to successful responses (issue #16).

## v12.14.0
- Slight refactor of `Auto***` filters to make usage simpler. May involve a rearrangement of implicit parameters (previously 
included the `Status`) depending on the use-case, but in majority of cases should not be breaking.
- Extended auto-marshalling support for XML responses to `Elem`. See `XML.Filters.Auto***`.
- Filter to allow multiple multiple body types to be accepted on a single route.

## v12.13.0
- Added auto-marshalling support for JSON libraries which support it (`Circe, Argonaut, Json4S, Play`. 
See `Circe.Filters.Auto***` filter constructors to wrap your existing domain services, or see `Circe` example for exact usage.

## v12.12.0
- Upgrade Finagle dependency to `6.35.0`. Note that this is (apparently) the last Scala `2.10.X` supporting Finagle release.

## v12.11.0
- Added `HeapDump` utility service to programatically generate a hprof file.
- Similar to `Query`, added support for multiple header parameters with same name. See `*()` on `Header.required|optional`.
- Support for serving strict content-type negotiation and multiple content types to be served from a single route. See example `strictcontenttypes`.
- Added a number of useful general-case filters. See `Filters`.
- Added a number of useful cache-control filters. See `Caching`.
- Upgrade of `Circe` version to `0.4.1`
- Upgrade of `Scala` version to `2.11.8`

## v12.10.0
- Renamed and moved implicit method conversions for response building for clarity from `X.toFuture` to `X.implicits.XXXToFuture`. Possible small break if you've imported the implicits exactly instead of with a wildcard.

## v12.8.0
- Added convenience methods for creating `ResponseSpec` object for auto-encoding JSON libraries.

## v12.7.0
- Added generification of ModuleSpec in the `Request` type as well as the `Response` type. This means that we can plug in other Finagle frameworks which vary in their Request type, such as Finagle-OAuth2. Likelihood of breaks is small, so 
just a minor version bump. In all cases, just replace `ModuleSpec[X]` and `ServerRoute[X]` with `ModuleSpec[Request, X]` and `ServerRoute[Request, X]` .
- Upgrade Finagle dependency to `6.34.0`
- Added examples for templating and OAuth2 integration

## v12.6.0
- Bolstered infra utilities to be friendlier for testing applications outside of a container

## v12.5.0
- Added UUID parameter type

## v12.4.0
- For performance, all JSON responses are now rendered in compact format by default when using a JSON library and the native node type (as opposed to string). This shouldn't really break anything in your code unless you're relying on comparisons 
with hardcoded JSON strings - :) 

## v12.3.0
- Added symbol support to `JsonFormat` object generation - see `objSym()`
- Upgrade of `Circe` to `0.3.0` - note the rename of the `circe-parse` package dependency to `circe-parser`

## v12.2.1
- Bugfix: Static resources not found when Static module is at the root context (issue #14).
- Upgrade finagle dependency to `6.33.0`

## v12.2.0
- Significant performance improvement by route matching on `ModuleSpec` context first and then on the whole path. This means that `ModuleSpec` contexts now 
CANNOT be shared at all, due to the routing matching just the Module context to select the Module and then then the rest of the path within that Module. For static resources, 
`StaticModule` instances that share their context with another (e.g. for dynamic web resources at the root context), need to come before any partner `ModuleSpec`
when creating the final service. Also, any overlapping ModuleSpecs will need to be combined into one.

## v12.1.0
- New documentation and project site

## v12.0.2
- Breaking change: Moved non-JSON ResponseBuilder classes to be consistent with other codecs. E.g. `io.fintrospect.formats.xml.XmlResponseBuilder` moves to `io.fintrospect.formats.Xml.ResponseBuilder`.
- (Unlikely) Breaking change: Rename of `ResponseBuilderMethods` trait to `AbstractResponseBuilder`.
- (Unlikely) Breaking change: Rename of `SprayJson` to `Spray` (for consistency).
- Deprecation of `FintrospectModule` to be replaced with `ModuleSpec` and `Module` for consistency.
- Added support Circe JSON library out of the box. See `Circe` to get the Format util and ResponseBuilder for this format.
- Added templating support for Mustache and Handlebars - similar to the JSON libraries, these require extra dependencies to be added to build.sbt.
- Added StaticModule for serving static resources from the classpath.
- Added SiteMapModuleRenderer for XML descriptions of web modules.
- Added parenthesis for `ResponseBuilder` `build()` method for consistency.

## v11.5.0
- Upgrade to Finagle `6.31.0`.
- Added ability to bind Scala `Option` to optional parameter values instead of just concrete values.
- Added `Security` to module endpoints, specifically `ApiKey` which adds a security filter to all requests. Documentation generation is included, but unfortunately the current Swagger UI does not support this properly due to a bug.
As a workaround until Swagger UI is fixed (if you need it), simply add the required ApiKey "parameter" to every endpoint, which will allow the best of both worlds (although you will not need to check for the presence of the API-Key in your 
routes since this will already be handled by Fintrospect.
- Added support GSON JSON library out of the box. See `Gson` to get the Format util and ResponseBuilder for this format.
- New example `full` and documentation rewrite to show off advanced feature usage of the library.

## v11.4.0
- Added convenience methods to remove boilerplate for auto marshall/demarshall JSON when creating body and parameters specs.

## v11.3.0
- Added convenience implicit conversions from `Status` to `ResponseBuilder` to `Response`. This tidies up the code nicely since you can use the Status as follows: `Status.Ok("content")`

## v11.2.1
- Bugfix: ArrayIndexOutOfBoundsException when handling an empty form (issue #12).

## v11.2.0
- Added using Buf's and ChannelBuffers to create HTTP responses.
- Removed deprecated methods from `ResponseBuilderMethods`.

## v11.1.1
- Added convenience mechanism to create custom parameters.

## v11.0.1
- Bugfix for ordering of the routes when matching a path (issue #11).

## v11.0.0
- Upgrade to Finagle `6.30.0`, which has resulted in... < drumroll >
- Breaking change: Musical chairs from the Finagle team with the `finagle-httpx` package, which is now renamed to `finagle-http`. Just globally replace `httpx` with `http` and all should be good.
- Removed `CorsFilter`. You can now use the `Cors.HttpFilter` supplied by finagle-http instead.

## v10.4.1
- Added 6 and 7 arity path lengths.

## v10.2.0
- Added support Play JSON library out of the box. See `Play` to get the Format util and ResponseBuilder for this format.

## v10.1.1
- Tiny break: Moved `ApiInfo` class to `io.fintrospect.renderers.swagger2dot0` package as was messing up the place.
- Inlined JsonResponseBuilder - this shouldn't be breaking anything - identically implement `JsonLibrary` instead for custom JSON formats.
- Added support Argonaut JSON library out of the box. See `Argonaut` to get the Format util and ResponseBuilder for this format.
- Added XHtml support (based on native XML).

## v10.0.1
- Bugfix for Json4S builder method.

## v10.0.0
- Upgrade from finagle-http v6.27.0 to finagle-httpx v6.29.0, as the former is EOL. This will result in a significant amount 
of breaking changes due to Finagle using their own httpx classes instead of Netty equivalents:
    - References to `HttpRequest/Response` Netty classes are now `Request/Response` instead
    - References to `HttpResponseStatus` changed to `Status`
    - References to `HttpMethod.GET/POST/...` changd to `Method.Get/Post/...`
    - References to `Http.XXX()` will now use `Httpx.XXX()` instead
- Other than the above, no actual changes to the Fintrospect API or how it works have been made.
- Added `OK` and `HttpResponse` alias methods to the `ResponseBuilder` to avoid name clashes with new HttpX methods. Deprecated `Ok()` and `Response()` methods for clarity.

## v9.1.0
- Changed format of "X-Fintrospect-Route-Name" header to use : instead of . in to describe the route URL - as URLs often have . in (for file extensions).

## v9.0.1
- Added support for other Scala JSON libraries - Json4S (Native and Jackson) and SprayJson out of the box. See `Json4s` to get the Format utils and ResponseBuilder for these formats.
- Breaking change: Response builders are now moved into the `io.fintrospect.formats.<format> packages`.
- Breaking change: rename of to `ArgoUtil` and `ArgoJsonResponseBuilder` which are now `Argo.JsonFormat` and `Argo.ResponseBuilder` respectively. This structure is now is unified with the other JSON formats.

## v9.0.0
- Please ignore this release and go straight to v9.0.1

## v8.3.1
- Bugfix for Body models not being outputted in Swagger 2 JSON schema.

## v8.3.0
- Added optional description field to `RouteSpec`. This appears as the "implementation notes" section in Swagger docs.

## v8.2.0
- Upgraded version of Finagle that we build against to v6.27.0.
- Bugfix for Path parameter values not being encoded correctly in clients.

## v8.1.0
- Added native XML support as a body and a parameter type.

## v8.0.1
- Bugfix for cannot bind custom body to a request.

## v8.0.X
- Unification of Server and Client route description APIs. Breaking API changes (renames only):
    - `DescribedRoute` is now `RouteSpec`
    - `ClientRoute` is now `RouteSpec` (different package), and `bindTo()` is now `bindToClient()`
    - `Route` is now `ServerRoute`
    - `Client` is now `RouteClient`
    - `ResponseWithExample` is now `ResponseSpec`
    - Methods on `Request/Response` have dropped the get prefix. Get `getUri -> uri`

## v7.6.X
- Support for multi-parameters in forms and queries to provide type-safe retrieval. Eg. `Query.required.*.int("name")`
- Removal of some generics around Parameters. Should be non-breaking change.

## v7.2.0
- Upgraded version of Finagle that we build against to v6.26.0

## v7.0.2
- Unfortunately contains a bug in the new Client code. Please ignore this release and upgrade to latest.

## v6.1.X -> v7.X.X
- Custom parameter support now more modular. Breaking change of `custom(XXX)` -> `apply(XXX)` in Parameters classes, which requires using ParameterSpec instead of arg list.
- Improved support for Forms and custom Body types (other than JSON). Dropped support for Forms parameters as request params and reimplemented as a particular Body type, with automatic setting of Content-Type
headers. Clients will need to migrate to the new Form format (see examples).
- Body parameters are now parsed for validity in the same way as other required parameters.
- EXPERIMENTAL! Ability to define clients using the same style API as the Services. Supports Path/Query/Headers, but not Bodies or Forms as yet.

## v5.X.X -> v6.X.X
- We've got a domain! So... repackage from `io.github.daviddenton.fintrospect` to  `io.fintrospect`. A global search and replace on your codebase will do it.
- Added support for custom parameter types. See the <a href="https://github.com/daviddenton/fintrospect/tree/master/src/test/scala/examples">example code]</a>).

## v4.X.X -> v5.X.X
- Upgrade to require Java 8 time API for time parameter support.
- Removal of Joda-time dependencies since you can now just use `java.time` instead.
- Collapsed `DescriptionRenderer` into `ModuleRenderer`, so just one trait to implement to provide custom format support.
- Ability to override the default location of the description route.

## v3.X.X -> v4.X.X

- Addition of custom response rendering formats. See the <a href="https://github.com/daviddenton/fintrospect/tree/master/src/test/scala/examples">example code]</a>.
  Modules now require a fully configured `ModuleRenderer` upon instantiation, which provides not just the rendering of the Documentation (ie. the old `Renderer` which is now `DescriptionRenderer`), but also the format of error messages. Some repackaging of the pluggable renderers has occurred.
- `ResponseBuilder` is now generic superclass of pluggable formats. Json support has moved to `JsonResponseBuilder`, so changes to your code will be required to support this.
- Renamed Identification Header to `X-Fintrospect-Route-Name`
- Added ability to apply filters to all custom routes in a module (apart from the description route, which is NOT affected)

## v2.X.X -> v3.X.X

Migrated away from the in-built Twitter HTTP Request package (`com.twitter.finagle.http`) and onto the Netty `
org.jboss.netty.handler.codec.http.Request`. This is to provide compatibility with the changes to the Finagle APIs in regards
to creating both servers and clients. It also has the advantage of unifying the client/server interface (previously it
was different between the two). The only things that should have to change in your code are:

  - How servers are created - the new method is simpler (see the <a href="https://github.com/daviddenton/fintrospect/tree/master/src/test/scala/examples">example code]</a>.
  - The signature of routes is now `Service[Request,Response]`. Since the Twitter Request/Response classes
   extends these interfaces, usages of the ResponseBuilder remain the same.
  - Form-based parameters are now defined with the `Form` object, and not the `Query` object (which now just retrieves Query String parameters from the URL).
