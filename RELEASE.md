Roadmap/Release Notes/Migration Guide
-----------------------------
The main API is fairly stable now, but expect some amount of breaking changes around major releases as new features are added.

######Master (in dev...)
- Strictness checks around accepted content types, resulting in  Unsupported Media Type (415) in case of mismatch.

#####v8.3.0
- Added optional description field to ```RouteSpec```. This appears as the "implementation notes" section in Swagger docs.

#####v8.2.0
- Upgraded version of Finagle that we build against to v6.27.0.
- Bugfix for Path parameter values not being encoded correctly in clients.

#####v8.1.0
- Added native XML support as a body and a parameter type.

#####v8.0.1
- Bugfix for cannot bind custom body to a request.

#####v8.0.X
- Unification of Server and Client route description APIs. Breaking API changes (renames only):
    - ```DescribedRoute``` is now ```RouteSpec```
    - ```ClientRoute``` is now ```RouteSpec``` (different package), and ```bindTo()``` is now ```bindToClient()```
    - ```Route``` is now ```ServerRoute```
    - ```Client``` is now ```RouteClient```
    - ```ResponseWithExample``` is now ```ResponseSpec```

#####v7.6.X
- Support for multi-parameters in forms and queries to provide type-safe retrieval. Eg. ```Query.required.*.int("name")```
- Removal of some generics around Parameters. Should be non-breaking change.

#####v7.2.0
- Upgraded version of Finagle that we build against to v6.26.0

#####v7.0.2
- Unfortunately contains a bug in the new Client code. Please ignore this release and upgrade to latest.

#####v6.1.X -> v7.X.X
- Custom parameter support now more modular. Breaking change of ```custom(XXX)``` -> ```apply(XXX)``` in Parameters classes, which requires using ParameterSpec instead of arg list.
- Improved support for Forms and custom Body types (other than JSON). Dropped support for Forms parameters as request params and reimplemented as a particular Body type, with automatic setting of Content-Type 
headers. Clients will need to migrate to the new Form format (see examples).
- Body parameters are now parsed for validity in the same way as other required parameters.
- EXPERIMENTAL! Ability to define clients using the same style API as the Services. Supports Path/Query/Headers, but not Bodies or Forms as yet.

#####v5.X.X -> v6.X.X
- We've got a domain! So... repackage from ```io.github.daviddenton.fintrospect``` to  ```fintrospect.io```. A global search and replace on your codebase will do it.
- Added support for custom parameter types. See the [example code](https://github.com/daviddenton/fintrospect/tree/master/src/test/scala/examples)).

#####v4.X.X -> v5.X.X
- Upgrade to require Java 8 time API for time parameter support.
- Removal of Joda-time dependencies since you can now just use ```java.time``` instead.
- Collapsed ```DescriptionRenderer``` into ```ModuleRenderer```, so just one trait to implement to provide custom format support.π
- Ability to override the default location of the description route.

#####v3.X.X -> v4.X.X

- Addition of custom response rendering formats. See the [example code](https://github.com/daviddenton/fintrospect/tree/master/src/test/scala/examples)).
  Modules now require a fully configured ```ModuleRenderer``` upon instantiation, which provides not just the rendering of the Documentation (ie. the old ```Renderer``` which is now ```DescriptionRenderer```), but also the format of error messages. Some repackaging of the pluggable renderers has occurred.
- ```ResponseBuilder``` is now generic superclass of pluggable formats. Json support has moved to ```JsonResponseBuilder```, so changes to your code will be required to support this.
- Renamed Identification Header to ```X-Fintrospect-Route-Name```
- Added ability to apply filters to all custom routes in a module (apart from the description route, which is NOT affected)

#####v2.X.X -> v3.X.X

Migrated away from the in-built Twitter HTTP Request package (```com.twitter.finagle.http```) and onto the Netty ```
org.jboss.netty.handler.codec.http.HttpRequest```. This is to provide compatibility with the changes to the Finagle APIs in regards
to creating both servers and clients. It also has the advantage of unifying the client/server interface (previously it
was different between the two). The only things that should have to change in your code are:

  - How servers are created - the new method is simpler (see the [example code](https://github.com/daviddenton/fintrospect/tree/master/src/test/scala/examples)).
  - The signature of routes is now ```Service[HttpRequest,HttpResponse]```. Since the Twitter Request/Response classes
   extends these interfaces, usages of the ResponseBuilder remain the same.
  - Form-based parameters are now defined with the ```Form``` object, and not the ```Query``` object (which now just retrieves Query String parameters from the URL).
