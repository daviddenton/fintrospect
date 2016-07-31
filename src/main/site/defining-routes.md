# defining routes
A ```RouteSpec()``` call starts to defines the specification of the contract (in terms of the required parameters) and the API follows the immutable 
builder pattern. Apart from the path-building elements (which terminate the builder), all of the "builder-y" calls here are optional, as 
are the descriptive strings (used for the auto-documenting features). Here's the simplest possible REST-like example for getting all employees
in a notional system:

```
RouteSpec().at(Method.Get) / "employee"
```

Notice that the request routing in that example was completely static? If we want an example of a dynamic endpoint, such as listing 
all users in a particular numerically-identified department, then we can introduce a ```Path``` parameter:
```
RouteSpec("list all employees in a particular group").at(Method.Get) / "employee" / Path.integer("departmentId")
```
... and we can do the same for Header and Query parameters; both optional and mandatory parameters are supported, as are parameters that can appear multiple times.:
```
RouteSpec("list all employees in a particular group")
    .taking(Header.optional.boolean("listOnlyActive"))
    .taking(Query.required.*.localDate("datesTakenAsHoliday"))
    .at(Method.Get) / "employee" / Path.integer("departmentId")
```
Moving onto HTTP bodies - for example adding an employee via a HTTP Post and declaring the content types that we produce (although 
this is optional):
```
RouteSpec("add employee", "Insert a new employee, failing if it already exists")
    .producing(ContentTypes.TEXT_PLAIN)
    .body(Body.form(FormField.required.string("name"), FormField.required.localDate("dateOfBirth")))
    .at(Method.Post) / "user" / Path.integer("departmentId")
```
  ... or via a form submission and declaring possible responses:
```
RouteSpec("add user", "Insert a new employee, failing if it already exists")
    .body(Body.form(FormField.required.string("name"), FormField.required.localDate("dateOfBirth")))
    .returning(Created -> "Employee was created")
    .returning(Conflict -> "Employee already exists")
    .at(Method.Post) / "user" / Path.integer("departmentId")
```

### using routes
As can be seen above, there are several stages to defining a route. Here is the complete construction lifecycle:
1. Create a `RouteSpec` with a name and description
2. Add details of parameters, any body, media-types and possible responses
3. Finalise the `RouteSpec` with a call to `at(<Method>)`. This creates an `UnboundRoute`.
4. Continue to add static or dynamic `Path` parameters to the URL structure (creating `UnboundRoute<n>` instances).

Once the final ```UnboundRoute``` has been created (with all of it's `Path` parts declared), it represents an HTTP contract, which can 
then be bound to:
1. an HTTP <a href="server-routes">server</a> `Service` if you wish to serve that contract to other systems.
2. an HTTP <a href="client-routes">client</a> `Service` if you wish to consume that contract from a remote system.

<a class="next" href="http://fintrospect.io/server-routes"><button type="button" class="btn btn-sm btn-default">next: server routes</button></a>
