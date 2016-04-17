<h1 class="githubonly">Roadmap/Release Notes/Migration Guide</h1>

The main API is stable now, but expect some amount of breaking changes around major releases as new features are added. All breaking changes are documented with a migration step where possible.

## Backlog/upcoming
- Option to dynamically reload View templates
- Vetting of Accept and Content-types (return 418 for unsupported media type)
- Drop support for Scala 2.10 (when finagle.http does)

## v12.11.0 (uncut)
- Added `HeapDump` utility service to programatically generate a hprof file.
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
- Added support for custom parameter types. See the [example code](https://github.com/daviddenton/fintrospect/tree/master/src/test/scala/examples)).

## v4.X.X -> v5.X.X
- Upgrade to require Java 8 time API for time parameter support.
- Removal of Joda-time dependencies since you can now just use `java.time` instead.
- Collapsed `DescriptionRenderer` into `ModuleRenderer`, so just one trait to implement to provide custom format support.
- Ability to override the default location of the description route.

## v3.X.X -> v4.X.X

- Addition of custom response rendering formats. See the [example code](https://github.com/daviddenton/fintrospect/tree/master/src/test/scala/examples)).
  Modules now require a fully configured `ModuleRenderer` upon instantiation, which provides not just the rendering of the Documentation (ie. the old `Renderer` which is now `DescriptionRenderer`), but also the format of error messages. Some repackaging of the pluggable renderers has occurred.
- `ResponseBuilder` is now generic superclass of pluggable formats. Json support has moved to `JsonResponseBuilder`, so changes to your code will be required to support this.
- Renamed Identification Header to `X-Fintrospect-Route-Name`
- Added ability to apply filters to all custom routes in a module (apart from the description route, which is NOT affected)

## v2.X.X -> v3.X.X

Migrated away from the in-built Twitter HTTP Request package (`com.twitter.finagle.http`) and onto the Netty `
org.jboss.netty.handler.codec.http.Request`. This is to provide compatibility with the changes to the Finagle APIs in regards
to creating both servers and clients. It also has the advantage of unifying the client/server interface (previously it
was different between the two). The only things that should have to change in your code are:

  - How servers are created - the new method is simpler (see the [example code](https://github.com/daviddenton/fintrospect/tree/master/src/test/scala/examples)).
  - The signature of routes is now `Service[Request,Response]`. Since the Twitter Request/Response classes
   extends these interfaces, usages of the ResponseBuilder remain the same.
  - Form-based parameters are now defined with the `Form` object, and not the `Query` object (which now just retrieves Query String parameters from the URL).
